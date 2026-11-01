import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { createPortal } from 'react-dom';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
    createRoom,
    getRoomDetails,
    getRoomErrorMessage,
    getRoomSettings,
    getRooms,
    getRoomWebSocketUrl,
    kickRoomMember,
    leaveRoom,
    joinRoom,
    startBattle,
    switchRoomPosition,
    updateRoomSettings,
} from '../services/roomService';
import '../styles/CustomRooms.css';

const TEAM_NAMES = { A: 'Team A', B: 'Team B' };
const ROOM_CAPACITY = 8;
const ROOM_DIFFICULTIES = ['EASY', 'MEDIUM', 'HARD'];
const ROOM_PROBLEM_COUNTS = [1, 3, 5, 7, 9, 11, 13, 15];
const ROOM_TOPICS = [
    'ARRAYS',
    'STRINGS',
    'LINKED_LISTS',
    'STACKS_QUEUES',
    'HASHING',
    'TREES',
    'GRAPHS',
    'SORTING',
    'SEARCHING',
    'RECURSION',
    'DYNAMIC_PROGRAMMING',
];
const ROOM_TOPIC_LABELS = {
    LINKED_LISTS: 'Linked Lists',
    STACKS_QUEUES: 'Stacks & Queues',
    DYNAMIC_PROGRAMMING: 'Dynamic Programming',
};
let nextRoomStompClientId = 1;
let activeRoomStompClients = 0;
let nextRoomWebSocketId = 1;
let activeRoomWebSockets = 0;
const pendingRoomDetailRequests = new Map();

const getRoomDetailsOnce = (roomCode, userId, config) => {
    const requestKey = `${userId}:${roomCode}`;
    const pendingRequest = pendingRoomDetailRequests.get(requestKey);
    if (pendingRequest) return pendingRequest;

    const request = getRoomDetails(roomCode, config).finally(() => {
        if (pendingRoomDetailRequests.get(requestKey) === request) {
            pendingRoomDetailRequests.delete(requestKey);
        }
    });
    pendingRoomDetailRequests.set(requestKey, request);
    return request;
};

// Temporary development diagnostics; remove after capturing the browser frame.
const logRoomTransportDiagnostic = (event, details) => {
    if (import.meta.env.DEV) {
        console.info(`[room-transport] ${event}`, details);
    }
};

const getJwtSubject = (token) => {
    try {
        const payload = token.split('.')[1];
        if (!payload) return null;
        const base64 = payload
            .replace(/-/g, '+')
            .replace(/_/g, '/')
            .padEnd(Math.ceil(payload.length / 4) * 4, '=');
        return JSON.parse(atob(base64)).sub ?? null;
    } catch {
        return null;
    }
};

const getRoomStatus = (room) => room.roomStatus || room.status || 'WAITING';

const normalizeRoomSettings = (settings, roomCode) => {
    if (
        !settings
        || String(settings.roomCode) !== String(roomCode)
        || !ROOM_DIFFICULTIES.includes(settings.difficulty)
        || !ROOM_PROBLEM_COUNTS.includes(settings.numberOfProblems)
        || !Array.isArray(settings.topics)
        || settings.topics.length === 0
        || settings.topics.some((topic) => !ROOM_TOPICS.includes(topic))
    ) {
        return null;
    }

    return {
        roomCode: settings.roomCode,
        difficulty: settings.difficulty,
        numberOfProblems: settings.numberOfProblems,
        topics: ROOM_TOPICS.filter((topic) => settings.topics.includes(topic)),
    };
};

const getRoomTopicLabel = (topic) =>
    ROOM_TOPIC_LABELS[topic] || topic.replaceAll('_', ' ').toLowerCase()
        .replace(/\b\w/g, (letter) => letter.toUpperCase());

const RoomCard = ({ room, joining, onJoin }) => {
    const status = getRoomStatus(room);
    const canJoin = status === 'WAITING';
    const joinLabel = {
        WAITING: 'Join room',
        COUNTDOWN: 'Countdown in progress',
        IN_BATTLE: 'In battle',
        CLOSED: 'Room ended',
    }[status] || status.replaceAll('_', ' ');

    return (
        <article className="room-card">
            <div className="room-card-info">
                <div className="room-card-title">
                    <h2>{room.roomName}</h2>
                    <span className={`room-status room-status--${status.toLowerCase()}`}>
                        <span className="room-status-dot" aria-hidden="true" />
                        {status.replace('_', ' ')}
                    </span>
                </div>
                <p className="room-card-host">
                    Hosted by <strong>{room.hostDisplayName || 'Unknown player'}</strong>
                </p>
                <p className="room-id">
                    ROOM ID <strong>{room.roomCode}</strong>
                </p>
            </div>
            <button
                className="room-join-button"
                type="button"
                disabled={!canJoin || joining}
                onClick={() => onJoin(room.roomCode)}
            >
                {joining ? 'Joining…' : joinLabel}
            </button>
        </article>
    );
};

const PlayerSlot = ({
    member,
    isHost,
    isCurrentPlayer,
    slotNumber,
    onKick,
    kicking,
    onSelect,
    disabled,
    team,
}) => {
    const className = `player-slot${member ? ' player-slot--occupied' : ' player-slot--open'}${isCurrentPlayer ? ' player-slot--self' : ''}`;
    const content = (
        <>
            <span className="player-slot-number">{String(slotNumber).padStart(2, '0')}</span>
            {member ? (
                <div className="player-slot-player">
                    <span className="player-avatar" aria-hidden="true">
                        {(member.displayName || '?').trim().charAt(0).toUpperCase()}
                    </span>
                    <span className="player-slot-name" title={member.displayName}>
                        {member.displayName || 'Player'}
                        {isCurrentPlayer && <span className="player-you">YOU</span>}
                    </span>
                    {isHost && <span className="host-badge">HOST</span>}
                    {onKick && (
                        <button
                            className="player-kick-button"
                            type="button"
                            disabled={kicking}
                            onClick={() => onKick(member)}
                            aria-label={`Kick ${member.displayName || 'player'}`}
                        >
                            {kicking ? '…' : 'Kick'}
                        </button>
                    )}
                </div>
            ) : (
                <span className="player-slot-empty">Open slot</span>
            )}
        </>
    );

    return member ? (
        <div className={className}>{content}</div>
    ) : (
        <button
            className={className}
            type="button"
            disabled={disabled}
            onClick={onSelect}
            aria-label={`Select empty Team ${team}, Slot ${slotNumber}`}
        >
            {content}
        </button>
    );
};

const CustomRooms = () => {
    const { user } = useAuth();
    const { roomCode: routeRoomCode } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const [rooms, setRooms] = useState([]);
    const [roomsLoading, setRoomsLoading] = useState(true);
    const [roomsError, setRoomsError] = useState('');
    const [createOpen, setCreateOpen] = useState(false);
    const [roomName, setRoomName] = useState('');
    const [roomPassword, setRoomPassword] = useState('');
    const [roomIdInput, setRoomIdInput] = useState('');
    const [joinPassword, setJoinPassword] = useState('');
    const [createError, setCreateError] = useState('');
    const [joinError, setJoinError] = useState('');
    const [room, setRoom] = useState(null);
    const [roomLoading, setRoomLoading] = useState(Boolean(routeRoomCode));
    const [busyAction, setBusyAction] = useState('');
    const [roomError, setRoomError] = useState('');
    const [positionError, setPositionError] = useState('');
    const [confirmation, setConfirmation] = useState(null);
    const [roomNotice, setRoomNotice] = useState('');
    const [connectionState, setConnectionState] = useState('idle');
    const [connectionError, setConnectionError] = useState('');
    const [lastUpdated, setLastUpdated] = useState(null);
    const [countdownStartedAt, setCountdownStartedAt] = useState(null);
    const [countdownNow, setCountdownNow] = useState(0);
    const [roomSettings, setRoomSettings] = useState(null);
    const [settingsDraftDifficulty, setSettingsDraftDifficulty] = useState('EASY');
    const [settingsDraftTopics, setSettingsDraftTopics] = useState([]);
    const [settingsDraftProblemCount, setSettingsDraftProblemCount] = useState(1);
    const [settingsLoading, setSettingsLoading] = useState(false);
    const [settingsSaving, setSettingsSaving] = useState(false);
    const [settingsError, setSettingsError] = useState('');
    const [search, setSearch] = useState('');
    const roomRef = useRef(null);
    const roomRefreshSequence = useRef(0);
    const settingsRequestSequence = useRef(0);
    const settingsEventSequence = useRef(0);
    const applyRoomSettings = useCallback((settings) => {
        setRoomSettings(settings);
        setSettingsDraftDifficulty(settings.difficulty);
        setSettingsDraftTopics(settings.topics);
        setSettingsDraftProblemCount(settings.numberOfProblems);
    }, []);
    const roomMatchesRoute = room
        && String(room.roomCode) === String(routeRoomCode);
    const activeRoomCode = roomMatchesRoute ? room.roomCode : null;
    const currentUserId = user?.userId;

    const loadRooms = useCallback(async (signal) => {
        setRoomsLoading(true);
        setRoomsError('');
        try {
            const response = await getRooms({ signal });
            setRooms(Array.isArray(response.data) ? response.data : []);
            setRoomsError('');
        } catch (error) {
            if (error.code !== 'ERR_CANCELED') {
                setRoomsError(getRoomErrorMessage(error));
            }
        } finally {
            if (!signal?.aborted) setRoomsLoading(false);
        }
    }, []);

    useEffect(() => {
        const controller = new AbortController();
        let active = true;
        const fetchRooms = async () => {
            try {
                const response = await getRooms({ signal: controller.signal });
                if (active) {
                    setRooms(Array.isArray(response.data) ? response.data : []);
                    setRoomsError('');
                }
            } catch (error) {
                if (active && error.code !== 'ERR_CANCELED') {
                    setRoomsError(getRoomErrorMessage(error));
                }
            } finally {
                if (active) setRoomsLoading(false);
            }
        };
        fetchRooms();
        return () => {
            active = false;
            controller.abort();
        };
    }, []);

    useEffect(() => {
        let active = true;
        const restoreRoom = async () => {
            if (!routeRoomCode) return;

            const createdRoom = location.state?.createdRoom;
            const canUseCreatedRoom = createdRoom
                && String(createdRoom.roomCode) === String(routeRoomCode)
                && String(createdRoom.hostUserId) === String(currentUserId);
            setRoomLoading(true);
            if (canUseCreatedRoom) {
                roomRef.current = createdRoom;
                setRoom(createdRoom);
            } else {
                setRoom(null);
                roomRef.current = null;
            }
            roomRefreshSequence.current += 1;
            setCountdownStartedAt(null);
            setRoomError('');

            if (!/^\d{1,10}$/.test(routeRoomCode)) {
                setRoomError('That room ID is not valid.');
                setRoomLoading(false);
                return;
            }

            try {
                const response = await getRoomDetailsOnce(routeRoomCode, currentUserId);
                if (!active) return;
                roomRef.current = response.data;
                setRoom(response.data);
                if (['IN_BATTLE', 'CLOSED'].includes(getRoomStatus(response.data))) {
                    setCountdownStartedAt(null);
                }
                setLastUpdated(new Date());
            } catch (error) {
                if (active && error.code !== 'ERR_CANCELED') {
                    setRoomError(getRoomErrorMessage(error));
                }
            } finally {
                if (active) setRoomLoading(false);
            }
        };

        restoreRoom();
        return () => {
            active = false;
        };
    }, [routeRoomCode, currentUserId, location.state]);

    useEffect(() => {
        const requestSequence = ++settingsRequestSequence.current;
        if (!activeRoomCode) return undefined;

        const controller = new AbortController();
        const eventSequence = settingsEventSequence.current;

        const loadSettings = async () => {
            setRoomSettings(null);
            setSettingsDraftTopics([]);
            setSettingsLoading(true);
            setSettingsError('');
            try {
                const response = await getRoomSettings(activeRoomCode, {
                    signal: controller.signal,
                });
                if (
                    requestSequence !== settingsRequestSequence.current
                    || eventSequence !== settingsEventSequence.current
                ) return;

                const settings = normalizeRoomSettings(response.data, activeRoomCode);
                if (!settings) {
                    setSettingsError('The room service returned invalid settings. Try refreshing the room.');
                    return;
                }
                applyRoomSettings(settings);
            } catch (error) {
                if (
                    requestSequence === settingsRequestSequence.current
                    && error.code !== 'ERR_CANCELED'
                ) {
                    setSettingsError(getRoomErrorMessage(error));
                }
            } finally {
                if (requestSequence === settingsRequestSequence.current) {
                    setSettingsLoading(false);
                }
            }
        };

        loadSettings();
        return () => {
            controller.abort();
            if (requestSequence === settingsRequestSequence.current) {
                settingsRequestSequence.current += 1;
            }
        };
    }, [activeRoomCode, applyRoomSettings]);

    const refreshRoom = useCallback(async (signal) => {
        const activeRoom = roomRef.current;
        if (!activeRoom) return;

        const requestSequence = ++roomRefreshSequence.current;
        try {
            const response = await getRoomDetailsOnce(
                activeRoom.roomCode,
                currentUserId,
                { signal }
            );
            if (requestSequence !== roomRefreshSequence.current) return;
            roomRef.current = response.data;
            setRoom(response.data);
            if (['IN_BATTLE', 'CLOSED'].includes(getRoomStatus(response.data))) {
                setCountdownStartedAt(null);
            }
            setLastUpdated(new Date());
            setRoomError('');
            return response.data;
        } catch (error) {
            if (requestSequence !== roomRefreshSequence.current) return null;
            if (error.code !== 'ERR_CANCELED') {
                if (error.response?.status === 403) {
                    roomRefreshSequence.current += 1;
                    roomRef.current = null;
                    setRoom(null);
                    setRoomError('You no longer have access to this room. You may have been kicked or removed.');
                    setConnectionState('idle');
                } else {
                    setRoomError(getRoomErrorMessage(error));
                }
            }
            return null;
        }
    }, [currentUserId]);

    useEffect(() => {
        if (!activeRoomCode || countdownStartedAt === null) return undefined;

        const updateCountdown = () => {
            const now = Date.now();
            setCountdownNow(now);
        };

        updateCountdown();
        const interval = window.setInterval(updateCountdown, 100);
        return () => window.clearInterval(interval);
    }, [activeRoomCode, countdownStartedAt]);

    useEffect(() => {
        if (!activeRoomCode) return undefined;

        const token = localStorage.getItem('token');
        if (!token) {
            const disconnectedTimer = window.setTimeout(() => {
                setConnectionState('disconnected');
                setConnectionError('Sign in again to connect to this room.');
            }, 0);
            return () => window.clearTimeout(disconnectedTimer);
        }
        const tokenSubject = getJwtSubject(token);
        if (String(tokenSubject) !== String(currentUserId)) {
            logRoomTransportDiagnostic('stomp identity mismatch', {
                roomCode: activeRoomCode,
                authContextUserId: currentUserId,
                tokenSubject,
            });
            const mismatchTimer = window.setTimeout(() => {
                setConnectionState('disconnected');
                setConnectionError('Your active session changed. Refresh the page and sign in again to reconnect.');
            }, 0);
            return () => window.clearTimeout(mismatchTimer);
        }

        const connectingTimer = window.setTimeout(() => {
            setConnectionState('connecting');
            setConnectionError('');
        }, 0);
        let active = true;
        let roomEventsSubscription;
        const clientId = nextRoomStompClientId++;
        const endpoint = getRoomWebSocketUrl();
        const safeEndpointUrl = new URL(endpoint);
        const safeEndpoint = `${safeEndpointUrl.origin}${safeEndpointUrl.pathname}`;
        activeRoomStompClients += 1;
        logRoomTransportDiagnostic('client created', {
            clientId,
            roomCode: activeRoomCode,
            activeClients: activeRoomStompClients,
            endpoint: safeEndpoint,
        });
        const client = new Client({
            webSocketFactory: () => {
                const socketId = nextRoomWebSocketId++;
                const socket = new WebSocket(endpoint);
                activeRoomWebSockets += 1;
                logRoomTransportDiagnostic('websocket created', {
                    clientId,
                    socketId,
                    roomCode: activeRoomCode,
                    activeSockets: activeRoomWebSockets,
                });
                socket.addEventListener('open', () => {
                    logRoomTransportDiagnostic('websocket open', {
                        clientId,
                        socketId,
                        activeSockets: activeRoomWebSockets,
                    });
                });
                if (import.meta.env.DEV) {
                    const nativeSend = socket.send.bind(socket);
                    socket.send = (data) => {
                        if (typeof data === 'string' && data.startsWith('CONNECT\n')) {
                            const separatorIndex = data.indexOf('\n\n');
                            const headerLines = separatorIndex >= 0
                                ? data.slice(0, separatorIndex).split('\n').slice(1)
                                : [];
                            const hasBearerAuthorization = headerLines.some((line) => {
                                const separator = line.indexOf(':');
                                return separator >= 0
                                    && line.slice(0, separator).toLowerCase() === 'authorization'
                                    && line.slice(separator + 1).startsWith('Bearer ');
                            });
                            const body = separatorIndex >= 0
                                ? data.slice(separatorIndex + 2).replace(/\0$/, '')
                                : '';
                            logRoomTransportDiagnostic('outbound CONNECT frame', {
                                clientId,
                                socketId,
                                headerSeparatorPresent: separatorIndex >= 0,
                                bearerAuthorizationHeaderPresent: hasBearerAuthorization,
                                bodyBytes: new TextEncoder().encode(body).length,
                                nullTerminated: data.endsWith('\0'),
                            });
                        }
                        return nativeSend(data);
                    };
                }
                socket.addEventListener('close', (event) => {
                    activeRoomWebSockets = Math.max(0, activeRoomWebSockets - 1);
                    logRoomTransportDiagnostic('websocket close', {
                        clientId,
                        socketId,
                        closeCode: event.code,
                        activeSockets: activeRoomWebSockets,
                    });
                }, { once: true });
                return socket;
            },
            connectHeaders: { Authorization: `Bearer ${token}` },
            reconnectDelay: 5000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            onConnect: () => {
                if (!active) return;
                logRoomTransportDiagnostic('stomp connected', {
                    clientId,
                    roomCode: activeRoomCode,
                    authenticatedUserId: currentUserId,
                    tokenSubject,
                });
                roomEventsSubscription?.unsubscribe();
                roomEventsSubscription = client.subscribe('/user/queue/room-events', (frame) => {
                    if (!active) return;

                    let event;
                    try {
                        event = JSON.parse(frame.body);
                    } catch (error) {
                        logRoomTransportDiagnostic('room event JSON parse failed', {
                            clientId,
                            roomCode: activeRoomCode,
                            error: error instanceof Error ? error.message : 'unknown parse error',
                        });
                        return;
                    }

                    const eventType = event && typeof event === 'object'
                        ? event.eventType ?? event.type
                        : undefined;
                    const matchesUser = currentUserId != null
                        && String(event?.userId) === String(currentUserId);
                    const matchesRoom = String(event?.roomCode) === String(activeRoomCode);
                    logRoomTransportDiagnostic('room event received', {
                        clientId,
                        roomCode: activeRoomCode,
                        authenticatedUserId: currentUserId,
                        tokenSubject,
                        eventUserId: event?.userId,
                        eventRoomCode: event?.roomCode,
                        eventType,
                        matchesUser,
                        matchesRoom,
                        timestampPresent: typeof event?.message === 'string',
                    });

                    if (!event || typeof event !== 'object' || !matchesRoom) {
                        logRoomTransportDiagnostic('room event ignored', {
                            clientId,
                            reason: !matchesRoom ? 'room code mismatch' : 'invalid event payload',
                        });
                        return;
                    }

                    if (eventType === 'ROOM_SETTINGS_UPDATED') {
                        try {
                            if (typeof event.message !== 'string') {
                                throw new Error('Settings event message is not a JSON string');
                            }
                            const payload = JSON.parse(event.message);
                            const settings = normalizeRoomSettings(
                                { ...payload, roomCode: event.roomCode },
                                activeRoomCode
                            );
                            if (!settings) {
                                throw new Error('Settings event contains invalid room settings');
                            }
                            settingsEventSequence.current += 1;
                            applyRoomSettings(settings);
                            setSettingsError('');
                        } catch (error) {
                            logRoomTransportDiagnostic('room settings event ignored', {
                                clientId,
                                roomCode: activeRoomCode,
                                error: error instanceof Error ? error.message : 'invalid settings payload',
                            });
                        }
                        return;
                    }

                    if (!matchesUser) {
                        logRoomTransportDiagnostic('room event ignored', {
                            clientId,
                            reason: 'user id mismatch',
                            eventType,
                        });
                        return;
                    }

                    if (eventType === 'ROOM_UPDATED') {
                        refreshRoom();
                        return;
                    }

                    if (eventType === 'ROOM_CLOSED') {
                        roomRefreshSequence.current += 1;
                        roomRef.current = null;
                        setRoom(null);
                        setConnectionState('idle');
                        navigate('/custom-rooms', {
                            replace: true,
                            state: { roomNotice: 'The host closed this room.' },
                        });
                        loadRooms();
                        return;
                    }

                    if (eventType === 'PLAYER_KICKED') {
                        roomRefreshSequence.current += 1;
                        roomRef.current = null;
                        setRoom(null);
                        setConfirmation(null);
                        setRoomError('');
                        setRoomNotice('Host removed you from the room');
                        setConnectionState('idle');
                        navigate('/custom-rooms', {
                            replace: true,
                            state: { roomNotice: 'Host removed you from the room' },
                        });
                        loadRooms();
                        return;
                    }

                    if (eventType === 'BATTLE_COUNTDOWN' && typeof event.message === 'string') {
                        const startedAt = Date.parse(event.message);
                        logRoomTransportDiagnostic('battle countdown event evaluated', {
                            clientId,
                            roomCode: activeRoomCode,
                            authenticatedUserId: currentUserId,
                            tokenSubject,
                            eventType,
                            timestampValid: Number.isFinite(startedAt),
                            startTime: Number.isFinite(startedAt)
                                ? new Date(startedAt).toISOString()
                                : undefined,
                        });
                        if (Number.isFinite(startedAt)) {
                            roomRefreshSequence.current += 1;
                            const activeRoom = roomRef.current;
                            if (activeRoom) {
                                const countdownRoom = { ...activeRoom, roomStatus: 'COUNTDOWN' };
                                roomRef.current = countdownRoom;
                                setRoom(countdownRoom);
                            }
                            setCountdownNow(Date.now());
                            setCountdownStartedAt(startedAt);
                        }
                    }
                });
                logRoomTransportDiagnostic('room events subscribed', {
                    clientId,
                    roomCode: activeRoomCode,
                    destination: '/user/queue/room-events',
                    authenticatedUserId: currentUserId,
                });
                setConnectionState('connected');
                setConnectionError('');
                refreshRoom();
            },
            onWebSocketClose: () => {
                roomEventsSubscription = undefined;
                if (active) {
                    logRoomTransportDiagnostic('stomp reconnect scheduled', {
                        clientId,
                        roomCode: activeRoomCode,
                    });
                    setConnectionState('reconnecting');
                    setConnectionError('Room connection was lost. Retrying automatically…');
                }
            },
            onWebSocketError: () => {
                if (active) {
                    setConnectionState('disconnected');
                    setConnectionError('Could not connect to room transport. The client will retry automatically.');
                }
            },
            onStompError: (frame) => {
                if (active) {
                    setConnectionState('disconnected');
                    setConnectionError(
                        frame.headers.message
                            ? `Room transport rejected the connection: ${frame.headers.message}`
                            : 'Room transport rejected the connection. The client will retry automatically.'
                    );
                }
            },
        });

        client.activate();
        return () => {
            active = false;
            window.clearTimeout(connectingTimer);
            activeRoomStompClients = Math.max(0, activeRoomStompClients - 1);
            logRoomTransportDiagnostic('client cleanup', {
                clientId,
                roomCode: activeRoomCode,
                activeClients: activeRoomStompClients,
            });
            roomEventsSubscription?.unsubscribe();
            client.deactivate();
        };
    }, [activeRoomCode, applyRoomSettings, currentUserId, loadRooms, navigate, refreshRoom]);

    const filteredRooms = useMemo(() => {
        const query = search.trim().toLocaleLowerCase();
        if (!query) return rooms;
        return rooms.filter((item) =>
            `${item.roomName} ${item.roomCode} ${item.hostDisplayName}`
                .toLocaleLowerCase()
                .includes(query)
        );
    }, [rooms, search]);

    const members = room?.roomMembers || [];
    const roomStatus = room ? getRoomStatus(room) : 'WAITING';
    useEffect(() => {
        const battleHasEnded = ['COMPLETED', 'FINISHED', 'ENDED', 'CLOSED'].includes(roomStatus);
        if (routeRoomCode && room && (roomStatus === 'IN_BATTLE' || battleHasEnded)) {
            navigate(`/battle-workspace/${room.roomCode}`, { replace: true });
        }
    }, [navigate, room, roomStatus, routeRoomCode]);

    const isHost = String(room?.hostUserId) === String(user?.userId);
    const countdownSeconds = countdownStartedAt === null
        ? 0
        : Math.max(0, Math.ceil((countdownStartedAt + 3000 - countdownNow) / 1000));
    const currentMember = members.find(
        (member) => String(member.userId) === String(user?.userId)
    );
    const occupiedPositions = new Set(
        members.map((member) => `${member.team}-${member.slotNumber}`)
    );
    const joinByCode = async (roomCode, password = joinPassword) => {
        if (!/^\d{1,10}$/.test(String(roomCode))) {
            return 'Enter a valid numeric room ID.';
        }

        setBusyAction(String(roomCode));
        setJoinError('');
        try {
            await joinRoom(roomCode, password);
            setJoinPassword('');
            navigate(`/custom-rooms/${roomCode}`);
            return '';
        } catch (error) {
            return getRoomErrorMessage(error);
        } finally {
            setBusyAction('');
        }
    };

    const handleJoinByCode = async (event) => {
        event.preventDefault();
        const errorMessage = await joinByCode(roomIdInput.trim());
        if (errorMessage) setJoinError(errorMessage);
    };

    const handleCreate = async (event) => {
        event.preventDefault();
        setBusyAction('create');
        setCreateError('');
        try {
            const response = await createRoom({
                roomName: roomName.trim(),
                password: roomPassword || null,
            });
            const createdRoom = response.data;
            setCreateOpen(false);
            setRoomName('');
            setRoomPassword('');
            navigate(`/custom-rooms/${createdRoom.roomCode}`, {
                state: { createdRoom },
            });
        } catch (error) {
            setCreateError(getRoomErrorMessage(error));
        } finally {
            setBusyAction('');
        }
    };

    const handleJoinCard = async (roomCode) => {
        const errorMessage = await joinByCode(roomCode);
        if (errorMessage) setJoinError(errorMessage);
    };

    const handleSaveRoomSettings = async () => {
        if (
            !room
            || !isHost
            || roomStatus !== 'WAITING'
            || !roomSettings
            || settingsSaving
            || settingsLoading
            || settingsDraftTopics.length === 0
            || !ROOM_PROBLEM_COUNTS.includes(settingsDraftProblemCount)
        ) return;

        const eventSequence = settingsEventSequence.current;
        setSettingsSaving(true);
        setSettingsError('');
        try {
            const response = await updateRoomSettings(room.roomCode, {
                difficulty: settingsDraftDifficulty,
                topics: settingsDraftTopics,
                numberOfProblems: settingsDraftProblemCount,
            });
            const settings = normalizeRoomSettings(response.data, room.roomCode);
            if (!settings) {
                setSettingsError('The room service did not confirm the updated settings. Refresh and try again.');
            } else if (eventSequence === settingsEventSequence.current) {
                applyRoomSettings(settings);
            }
        } catch (error) {
            setSettingsError(getRoomErrorMessage(error));
        } finally {
            setSettingsSaving(false);
        }
    };

    const handleSwitchPosition = async () => {
        if (
            !room
            || roomStatus !== 'WAITING'
            || !currentMember
            || confirmation?.type !== 'position'
            || occupiedPositions.has(`${confirmation.team}-${confirmation.slotNumber}`)
            || busyAction
        ) return;

        setBusyAction('position');
        setPositionError('');
        try {
            await switchRoomPosition(room.roomCode, {
                team: confirmation.team,
                slotNumber: confirmation.slotNumber,
            });
            setConfirmation(null);
            const refreshedRoom = await refreshRoom();
            if (!refreshedRoom) {
                setPositionError('Your move was accepted, but the roster could not refresh yet. It will sync on the next room update.');
            }
        } catch (error) {
            setPositionError(getRoomErrorMessage(error));
            await refreshRoom();
        } finally {
            setBusyAction('');
        }
    };

    const handleLeaveRoom = async () => {
        if (!room) return;
        const leavingRoomCode = room.roomCode;
        setBusyAction('leave');
        setRoomError('');
        try {
            await leaveRoom(leavingRoomCode);
            roomRefreshSequence.current += 1;
            roomRef.current = null;
            setRoom(null);
            setConfirmation(null);
            setConnectionState('idle');
            navigate('/custom-rooms', {
                replace: true,
                state: {
                    roomNotice: String(room.hostUserId) === String(user?.userId)
                        ? 'Room closed. You were the host, so the room was deleted.'
                        : 'You left the room.',
                },
            });
            await loadRooms();
        } catch (error) {
            setRoomError(getRoomErrorMessage(error));
        } finally {
            setBusyAction('');
        }
    };

    const handleKickMember = async (targetMember) => {
        if (!room || String(room.hostUserId) !== String(user?.userId)) return;
        setBusyAction(`kick:${targetMember.userId}`);
        setRoomError('');
        try {
            await kickRoomMember(room.roomCode, targetMember.userId);
            setConfirmation(null);
            await refreshRoom();
        } catch (error) {
            setRoomError(getRoomErrorMessage(error));
        } finally {
            setBusyAction('');
        }
    };

    const handleStartBattle = async () => {
        if (!room || !isHost || roomStatus !== 'WAITING'
            || connectionState !== 'connected' || busyAction) return;

        setBusyAction('start');
        setRoomError('');
        try {
            await startBattle(room.roomCode);
        } catch (error) {
            setRoomError(getRoomErrorMessage(error));
        } finally {
            setBusyAction('');
        }
    };

    const copyRoomCode = async () => {
        try {
            await navigator.clipboard.writeText(String(room.roomCode));
        } catch {
            setRoomError('Could not copy the room ID. Select and copy it manually.');
        }
    };

    if (routeRoomCode && !roomMatchesRoute) {
        const restoring = roomLoading || Boolean(room);
        return (
            <main className="custom-rooms-page">
                <Link className="rooms-back-link rooms-lobby-back" to="/custom-rooms">
                    <span aria-hidden="true">←</span> Room browser
                </Link>
                <div className="rooms-state-card room-restore-state" role={restoring ? 'status' : 'alert'}>
                    {restoring ? (
                        <>
                            <span className="rooms-spinner" aria-hidden="true" />
                            <h3>Restoring your lobby</h3>
                            <p>Checking room access with the server…</p>
                        </>
                    ) : (
                        <>
                            <span className="rooms-state-icon" aria-hidden="true">!</span>
                            <h3>Couldn’t open this room</h3>
                            <p>{roomError || 'Room details are unavailable.'}</p>
                            <Link className="rooms-secondary-button" to="/custom-rooms">Return to room browser</Link>
                        </>
                    )}
                </div>
            </main>
        );
    }

    if (roomMatchesRoute) {
        const roomEnded = roomStatus === 'CLOSED';
        const connectionLabel = {
            connecting: 'Connecting to room transport',
            connected: 'Transport connected',
            reconnecting: 'Reconnecting to room transport',
            disconnected: 'Room transport disconnected',
            idle: 'Room transport disconnected',
        }[connectionState];

        return (
            <main className="custom-rooms-page custom-rooms-lobby custom-room-standalone">
                <header className="lobby-header">
                    <div className="lobby-heading">
                        <span className="rooms-eyebrow">CUSTOM ARENA / LOBBY</span>
                        <h1>{room.roomName}</h1>
                        <p>Get your squad in position. The host can start the battle when the room is ready.</p>
                    </div>
                    <div className="room-code-card">
                        <span>ROOM ID</span>
                        <strong>{room.roomCode}</strong>
                        <button type="button" onClick={copyRoomCode}>Copy ID</button>
                    </div>
                </header>

                <div className="lobby-actions">
                    <span className="lobby-host-note">
                        {String(room.hostUserId) === String(user?.userId)
                            ? 'Host controls enabled'
                            : 'Only the host can remove other players'}
                    </span>
                    <button
                        className="rooms-leave-button"
                        type="button"
                        onClick={() => setConfirmation({ type: 'leave' })}
                    >
                        Exit Room
                    </button>
                    {isHost && (
                        <button
                            className="rooms-create-button"
                            type="button"
                            onClick={handleStartBattle}
                            disabled={roomStatus !== 'WAITING'
                                || connectionState !== 'connected'
                                || settingsSaving
                                || Boolean(busyAction)}
                        >
                            {busyAction === 'start'
                                ? 'Starting…'
                                : roomStatus === 'COUNTDOWN'
                                    ? 'Countdown in progress'
                                    : roomStatus === 'IN_BATTLE'
                                        ? 'Battle started'
                                        : connectionState === 'connecting'
                                            ? 'Connecting to room…'
                                            : connectionState === 'reconnecting'
                                                ? 'Reconnecting…'
                                                : connectionState !== 'connected'
                                                    ? 'Room connection unavailable'
                                                    : 'Start battle'}
                        </button>
                    )}
                </div>

                {roomStatus === 'COUNTDOWN' && countdownStartedAt === null ? (
                    <p className="rooms-notice" role="status">
                        Countdown is in progress. Waiting for the synchronized countdown event.
                    </p>
                ) : roomStatus === 'IN_BATTLE' ? (
                    <p className="rooms-notice" role="status">
                        The server confirmed the battle has started. A battle workspace is not available in this frontend yet.
                    </p>
                ) : null}

                <div className="lobby-status-row">
                    <span className={`connection-indicator connection-indicator--${connectionState}`}>
                        <span aria-hidden="true" />
                        {connectionLabel}
                    </span>
                    <span className="lobby-refresh-note">
                        Lobby state syncs through room events
                        {lastUpdated && ` · Updated ${lastUpdated.toLocaleTimeString()}`}
                    </span>
                </div>
                {connectionError && <p className="rooms-error" role="status">{connectionError}</p>}
                <p className="rooms-capability-note">
                    {roomEnded
                        ? 'This room has ended. The server-provided roster is available for reference.'
                        : 'Room membership, seat, settings, and battle changes sync through authenticated room events.'}
                </p>

                {roomError && <p className="rooms-error" role="alert">{roomError}</p>}

                <section className="team-grid" aria-label="Room teams">
                    {['A', 'B'].map((team) => (
                        <article className={`team-panel team-panel--${team.toLowerCase()}`} key={team}>
                            <header className="team-panel-header">
                                <div>
                                    <span className="team-overline">SQUAD</span>
                                    <h2>{TEAM_NAMES[team]}</h2>
                                </div>
                                <span className="team-count">
                                    {members.filter((member) => member.team === team).length}/4
                                </span>
                            </header>
                            <div className="team-slots">
                                {[1, 2, 3, 4].map((slotNumber) => {
                                    const member = members.find(
                                        (player) => player.team === team && player.slotNumber === slotNumber
                                    );
                                    return (
                                        <PlayerSlot
                                            key={`${team}-${slotNumber}`}
                                            member={member}
                                            slotNumber={slotNumber}
                                            isHost={Boolean(member && String(member.userId) === String(room.hostUserId))}
                                            isCurrentPlayer={Boolean(member && String(member.userId) === String(user?.userId))}
                                            onKick={member
                                                && String(room.hostUserId) === String(user?.userId)
                                                && String(member.userId) !== String(user?.userId)
                                                ? () => setConfirmation({ type: 'kick', member })
                                                : null}
                                            kicking={busyAction === `kick:${member?.userId}`}
                                            team={team}
                                            disabled={!currentMember || roomStatus !== 'WAITING' || Boolean(busyAction)}
                                            onSelect={() => {
                                                setPositionError('');
                                                setConfirmation({ type: 'position', team, slotNumber });
                                            }}
                                        />
                                    );
                                })}
                            </div>
                        </article>
                    ))}
                </section>

                <section className="room-settings-panel" aria-labelledby="room-settings-title">
                    <header className="room-settings-header">
                        <div>
                            <span className="rooms-eyebrow">BATTLE RULES</span>
                            <h2 id="room-settings-title">Room settings</h2>
                            <p>
                                {isHost
                                    ? roomStatus === 'WAITING'
                                        ? 'Choose the difficulty and topics for this room.'
                                        : 'Room settings are locked after the battle starts.'
                                    : 'Only the room host can change these settings.'}
                            </p>
                        </div>
                        {isHost && (
                            <button
                                className="room-settings-save"
                                type="button"
                                onClick={handleSaveRoomSettings}
                                disabled={settingsLoading
                                    || settingsSaving
                                    || roomStatus !== 'WAITING'
                                    || settingsDraftTopics.length === 0
                                    || !ROOM_PROBLEM_COUNTS.includes(settingsDraftProblemCount)
                                    || !roomSettings}
                            >
                                {settingsSaving ? 'Saving…' : 'Save settings'}
                            </button>
                        )}
                    </header>

                    {settingsLoading ? (
                        <p className="room-settings-state" role="status">Loading saved settings…</p>
                    ) : roomSettings ? (
                        <>
                            <div className="room-settings-saved" aria-live="polite">
                                <span>
                                    Saved difficulty <strong>{roomSettings.difficulty}</strong>
                                </span>
                                <span>
                                    Saved problem count <strong>{roomSettings.numberOfProblems}</strong>
                                </span>
                                <div className="room-settings-saved-topics">
                                    <span>Saved topics</span>
                                    <div>
                                        {roomSettings.topics.map((topic) => (
                                            <span className="room-setting-tag" key={topic}>
                                                {getRoomTopicLabel(topic)}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            <div className="room-settings-editor">
                                <label className="room-settings-field" htmlFor="room-settings-difficulty">
                                    <span>Difficulty</span>
                                    <select
                                        id="room-settings-difficulty"
                                        value={settingsDraftDifficulty}
                                        disabled={!isHost || roomStatus !== 'WAITING' || settingsSaving}
                                        onChange={(event) => {
                                            setSettingsDraftDifficulty(event.target.value);
                                            setSettingsError('');
                                        }}
                                    >
                                        {ROOM_DIFFICULTIES.map((difficulty) => (
                                            <option key={difficulty} value={difficulty}>{difficulty}</option>
                                        ))}
                                    </select>
                                </label>

                                <label className="room-settings-field" htmlFor="room-settings-problem-count">
                                    <span>Number of problems</span>
                                    <select
                                        id="room-settings-problem-count"
                                        value={settingsDraftProblemCount}
                                        disabled={!isHost || roomStatus !== 'WAITING' || settingsSaving}
                                        onChange={(event) => {
                                            setSettingsDraftProblemCount(Number(event.target.value));
                                            setSettingsError('');
                                        }}
                                    >
                                        {ROOM_PROBLEM_COUNTS.map((count) => (
                                            <option key={count} value={count}>{count}</option>
                                        ))}
                                    </select>
                                </label>

                                <fieldset
                                    className="room-settings-topics"
                                    disabled={!isHost || roomStatus !== 'WAITING' || settingsSaving}
                                >
                                    <legend>Topics</legend>
                                    <div className="room-settings-topic-list">
                                        {ROOM_TOPICS.map((topic) => (
                                            <label className="room-settings-topic" key={topic}>
                                                <input
                                                    type="checkbox"
                                                    checked={settingsDraftTopics.includes(topic)}
                                                    onChange={(event) => {
                                                        setSettingsDraftTopics((previous) =>
                                                            event.target.checked
                                                                ? [...previous, topic]
                                                                : previous.filter((item) => item !== topic)
                                                        );
                                                        setSettingsError('');
                                                    }}
                                                />
                                                <span>{getRoomTopicLabel(topic)}</span>
                                            </label>
                                        ))}
                                    </div>
                                </fieldset>
                            </div>
                            {isHost && settingsDraftTopics.length === 0 && (
                                <p className="rooms-error" role="alert">
                                    Select at least one topic before saving.
                                </p>
                            )}
                        </>
                    ) : (
                        <p className="room-settings-state">
                            Saved settings are unavailable until the room settings service responds.
                        </p>
                    )}
                    {settingsError && <p className="rooms-error" role="alert">{settingsError}</p>}
                </section>

                {countdownStartedAt !== null && roomStatus !== 'IN_BATTLE'
                    && createPortal(
                        <div className="battle-countdown-overlay" role="status" aria-live="assertive">
                            <div className="battle-countdown-content">
                                <span className="battle-countdown-title">BATTLE STARTING</span>
                                <strong
                                    key={countdownSeconds}
                                    className="battle-countdown-number"
                                >
                                    {countdownSeconds > 0 ? countdownSeconds : 'GO'}
                                </strong>
                                <span className="battle-countdown-subtitle">
                                    {countdownSeconds > 0 ? 'GET READY' : 'WAITING FOR BATTLE CONFIRMATION'}
                                </span>
                            </div>
                        </div>,
                        document.body
                    )}

                <section className="lobby-bottom-grid">
                    <article className="lobby-info-panel">
                        <div>
                            <span className="rooms-eyebrow">ROOM STATUS</span>
                            <h2>{room.roomStatus || 'WAITING'}</h2>
                            <p>
                                Hosted by <strong>{room.hostDisplayName || 'Unknown player'}</strong>
                                {room.createdAt && ` · Created ${new Date(room.createdAt).toLocaleString()}`}
                            </p>
                        </div>
                        <span className="lobby-player-total">
                            {members.length}<span> / {ROOM_CAPACITY}</span>
                            <small>players</small>
                        </span>
                    </article>

                    <article className="position-panel">
                        <div className="position-panel-heading">
                            <span className="rooms-eyebrow">YOUR SEAT</span>
                            <h2>{currentMember ? `${TEAM_NAMES[currentMember.team]} · Slot ${currentMember.slotNumber}` : 'Not in this lobby'}</h2>
                        </div>
                        <p className="position-unavailable">
                            {roomStatus === 'WAITING' && currentMember
                                ? 'Select any open seat above to request a move.'
                                : roomStatus === 'COUNTDOWN'
                                    ? 'Seat changes are unavailable during the countdown.'
                                    : roomStatus === 'IN_BATTLE'
                                        ? 'Seat changes are unavailable after the battle starts.'
                                        : roomEnded
                                            ? 'Seat changes are unavailable because this room has ended.'
                                            : 'Only room members can select a seat.'}
                        </p>
                        {busyAction === 'position' && (
                            <p className="position-unavailable" role="status">Moving to the selected seat…</p>
                        )}
                        {positionError && <p className="rooms-error" role="alert">{positionError}</p>}
                    </article>
                </section>
                {confirmation && (
                    <div className="rooms-dialog-backdrop" onMouseDown={(event) => {
                        if (event.target === event.currentTarget && !busyAction) setConfirmation(null);
                    }}>
                        <section
                            className="rooms-dialog rooms-confirm-dialog"
                            role="alertdialog"
                            aria-modal="true"
                            aria-labelledby="room-confirm-title"
                            aria-describedby="room-confirm-description"
                        >
                            <span className="rooms-dialog-eyebrow">
                                {confirmation.type === 'position'
                                    ? 'SEAT SELECTION'
                                    : confirmation.type === 'leave' ? 'ROOM EXIT' : 'HOST CONTROL'}
                            </span>
                            <h2 id="room-confirm-title">
                                {confirmation.type === 'position'
                                    ? 'Switch Slot?'
                                    : confirmation.type === 'leave'
                                        ? String(room.hostUserId) === String(user?.userId) ? 'Close this room?' : 'Leave this room?'
                                        : `Kick ${confirmation.member.displayName || 'this player'}?`}
                            </h2>
                            <p className="rooms-dialog-description" id="room-confirm-description">
                                {confirmation.type === 'position'
                                    ? `Do you want to move to ${TEAM_NAMES[confirmation.team]}, Slot ${confirmation.slotNumber}?`
                                    : confirmation.type === 'leave'
                                        ? String(room.hostUserId) === String(user?.userId)
                                            ? 'As the host, leaving deletes the room and removes every player. This cannot be undone.'
                                            : 'You will be removed from this room and will need to join again to return.'
                                        : 'This player will be removed from the room. They may rejoin if the room is still open.'}
                            </p>
                            {confirmation.type === 'position' && positionError && (
                                <p className="rooms-error" role="alert">{positionError}</p>
                            )}
                            {confirmation.type !== 'position' && roomError && (
                                <p className="rooms-error" role="alert">{roomError}</p>
                            )}
                            <div className="rooms-dialog-actions">
                                <button
                                    className="rooms-cancel-button"
                                    type="button"
                                    disabled={Boolean(busyAction)}
                                    onClick={() => {
                                        setConfirmation(null);
                                        setRoomError('');
                                        setPositionError('');
                                    }}
                                    autoFocus
                                >
                                    Cancel
                                </button>
                                <button
                                    className={confirmation.type === 'position' ? 'rooms-create-button' : 'rooms-danger-button'}
                                    type="button"
                                    disabled={Boolean(busyAction)}
                                    onClick={confirmation.type === 'position'
                                        ? handleSwitchPosition
                                        : confirmation.type === 'leave'
                                            ? handleLeaveRoom
                                            : () => handleKickMember(confirmation.member)}
                                >
                                    {confirmation.type === 'position'
                                        ? busyAction === 'position' ? 'Moving…' : 'OK'
                                        : busyAction === 'leave'
                                            ? 'Leaving…'
                                            : busyAction.startsWith('kick:')
                                                ? 'Kicking…'
                                                : confirmation.type === 'leave'
                                                ? 'Exit Room'
                                                    : 'Kick player'}
                                </button>
                            </div>
                        </section>
                    </div>
                )}
            </main>
        );
    }

    return (
        <main className="custom-rooms-page">
            <div className="rooms-back-row">
                <Link className="rooms-back-link" to="/dashboard">
                    <span aria-hidden="true">←</span> Back to dashboard
                </Link>
            </div>

            <header className="rooms-header">
                <div>
                    <span className="rooms-eyebrow">DSABG ARENA</span>
                    <h1>Custom Rooms</h1>
                    <p>Build your squad. Choose your side. Own the arena.</p>
                </div>
                <button className="rooms-create-button" type="button" onClick={() => {
                    setCreateError('');
                    setCreateOpen(true);
                }}>
                    <span aria-hidden="true">+</span> Create room
                </button>
            </header>

            <section className="rooms-quick-join" aria-label="Join a room by ID">
                <div className="quick-join-copy">
                    <span className="quick-join-icon" aria-hidden="true">⌁</span>
                    <div>
                        <h2>Got an invite?</h2>
                        <p>Enter the room ID and jump into the lobby.</p>
                    </div>
                </div>
                <form className="quick-join-form" onSubmit={handleJoinByCode}>
                    <label className="visually-hidden" htmlFor="room-id-input">Room ID</label>
                    <input
                        id="room-id-input"
                        type="text"
                        inputMode="numeric"
                        pattern="[0-9]{1,10}"
                        maxLength={10}
                        placeholder="Enter room ID"
                        value={roomIdInput}
                        onChange={(event) => {
                            setRoomIdInput(event.target.value.replace(/\D/g, ''));
                            setJoinError('');
                        }}
                        required
                    />
                    <label className="visually-hidden" htmlFor="join-room-password">Room password (if required)</label>
                    <input
                        id="join-room-password"
                        type="password"
                        autoComplete="current-password"
                        placeholder="Password (if required)"
                        value={joinPassword}
                        onChange={(event) => {
                            setJoinPassword(event.target.value);
                            setJoinError('');
                        }}
                    />
                    <button className="rooms-secondary-button" type="submit" disabled={!roomIdInput || Boolean(busyAction)}>
                        {busyAction && busyAction !== 'create' ? 'Joining…' : 'Join room'}
                    </button>
                </form>
                {joinError && <p className="rooms-error quick-join-error" role="alert">{joinError}</p>}
            </section>
            {(roomNotice || location.state?.roomNotice) && (
                <p className="rooms-notice" role="status">
                    {roomNotice || location.state.roomNotice}
                    <button
                        type="button"
                        onClick={() => {
                            setRoomNotice('');
                            navigate('/custom-rooms', { replace: true, state: null });
                        }}
                        aria-label="Dismiss message"
                    >×</button>
                </p>
            )}

            <section className="rooms-browser" aria-label="Active rooms">
                <div className="rooms-toolbar">
                    <div>
                        <span className="rooms-eyebrow">FIND YOUR NEXT MATCH</span>
                        <h2>Open lobbies <span>{rooms.length}</span></h2>
                    </div>
                    <div className="rooms-toolbar-actions">
                        <label className="rooms-search">
                            <span className="rooms-search-icon" aria-hidden="true">⌕</span>
                            <input
                                type="search"
                                placeholder="Search name or ID"
                                value={search}
                                onChange={(event) => setSearch(event.target.value)}
                            />
                        </label>
                        <button className="rooms-refresh-button" type="button" onClick={() => loadRooms()} aria-label="Refresh rooms">
                            ↻
                        </button>
                    </div>
                </div>

                {roomsLoading ? (
                    <div className="rooms-state-card" role="status">
                        <span className="rooms-spinner" aria-hidden="true" />
                        <h3>Scanning the arena</h3>
                        <p>Loading available rooms from the server…</p>
                    </div>
                ) : roomsError ? (
                    <div className="rooms-state-card rooms-state-card--error">
                        <span className="rooms-state-icon" aria-hidden="true">!</span>
                        <h3>Couldn’t load the room list</h3>
                        <p>{roomsError}</p>
                        <button className="rooms-secondary-button" type="button" onClick={() => {
                            setRoomsLoading(true);
                            loadRooms();
                        }}>Try again</button>
                    </div>
                ) : filteredRooms.length ? (
                    <div className="rooms-list" aria-live="polite">
                        {filteredRooms.map((item) => (
                            <RoomCard
                                key={item.roomCode}
                                room={item}
                                joining={busyAction === String(item.roomCode)}
                                onJoin={handleJoinCard}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="rooms-state-card">
                        <span className="rooms-state-icon" aria-hidden="true">⌕</span>
                        <h3>{search ? 'No matching rooms' : 'The arena is quiet'}</h3>
                        <p>{search ? 'Try another room name or ID.' : 'No active rooms are available right now. Create a room and bring your crew.'}</p>
                        {!search && (
                            <button className="rooms-secondary-button" type="button" onClick={() => setCreateOpen(true)}>
                                Create the first room
                            </button>
                        )}
                    </div>
                )}
            </section>

            <aside className="rooms-backend-notice">
                <span aria-hidden="true">i</span>
                <p>
                    Room passwords are verified by the service. Host-only kick and member leave/room-close actions are confirmed by the server.
                </p>
            </aside>

            {createOpen && (
                <div className="rooms-dialog-backdrop" onMouseDown={(event) => {
                    if (event.target === event.currentTarget && busyAction !== 'create') setCreateOpen(false);
                }}>
                    <section className="rooms-dialog" role="dialog" aria-modal="true" aria-labelledby="create-room-title">
                        <button className="rooms-dialog-close" type="button" onClick={() => setCreateOpen(false)} aria-label="Close dialog">×</button>
                        <span className="rooms-dialog-eyebrow">SET UP YOUR MATCH</span>
                        <h2 id="create-room-title">Create a room</h2>
                        <p className="rooms-dialog-description">Set the stage, then invite your squad with the room ID.</p>
                        <form onSubmit={handleCreate}>
                            <label htmlFor="room-name">Room name <span aria-hidden="true">*</span></label>
                            <input
                                id="room-name"
                                value={roomName}
                                onChange={(event) => setRoomName(event.target.value)}
                                maxLength={60}
                                placeholder="e.g. Friday Night Battle"
                                required
                            />
                            <label htmlFor="room-password">Password <span className="rooms-optional">(optional)</span></label>
                            <input
                                id="room-password"
                                type="password"
                                value={roomPassword}
                                onChange={(event) => setRoomPassword(event.target.value)}
                                autoComplete="new-password"
                                placeholder="Set a room password"
                            />
                            <p className="rooms-form-hint">
                                Players need this password to join a protected room.
                            </p>
                            <p className="rooms-form-hint">Up to 8 players · Team A and Team B · You join the lobby after creation.</p>
                            {createError && <p className="rooms-error" role="alert">{createError}</p>}
                            <div className="rooms-dialog-actions">
                                <button className="rooms-cancel-button" type="button" onClick={() => setCreateOpen(false)} disabled={busyAction === 'create'}>Cancel</button>
                                <button className="rooms-create-button" type="submit" disabled={!roomName.trim() || busyAction === 'create'}>
                                    {busyAction === 'create' ? 'Creating…' : 'Create room'}
                                </button>
                            </div>
                        </form>
                    </section>
                </div>
            )}
        </main>
    );
};

export default CustomRooms;
