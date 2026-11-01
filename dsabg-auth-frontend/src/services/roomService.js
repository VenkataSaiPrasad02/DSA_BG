import API from '../api/axios';

export const getRooms = (config) => API.get('/room/get-all-rooms', config);

export const createRoom = (data) => API.post('/room/create', data);

export const joinRoom = (roomCode, password = '') =>
    API.post('/room/join', { roomCode, password: password || null });

export const getRoomDetails = (roomCode, config) =>
    API.get(`/room/${roomCode}/get-your-room-details`, config);

export const getRoomSettings = (roomCode, config) =>
    API.get(`/room/${roomCode}/settings/get-settings`, config);

export const updateRoomSettings = (roomCode, settings) =>
    API.put(`/room/${roomCode}/settings/update-settings`, settings);

export const switchRoomPosition = (roomCode, position) =>
    API.patch(`/room/${roomCode}/switch-position`, position);

export const leaveRoom = (roomCode) => API.delete(`/room/${roomCode}/leave`);

export const kickRoomMember = (roomCode, targetUserId) =>
    API.delete(`/room/${roomCode}/members/${encodeURIComponent(targetUserId)}`);

export const startBattle = (roomCode) =>
    API.post(`/room/${roomCode}/start`);

export const getRoomErrorMessage = (error) => {
    const status = error.response?.status;
    const response = error.response?.data;
    const message = typeof response === 'string'
        ? response
        : response?.message || response?.detail || response?.error;

    if (status === 404) {
        return message && message !== 'Not Found'
            ? message
            : 'Room not found. Its ID may be invalid or the room may have ended.';
    }

    if (status === 401) {
        return 'Your session has expired. Sign in again to continue.';
    }

    if (status === 403) {
        return message || 'You do not have permission to access this room.';
    }

    if (status === 409) {
        return message || 'The room or selected seat changed. Refresh the lobby and try again.';
    }

    if (message) {
        return message;
    }

    if (error.request) {
        return 'The room service could not be reached. Check the API Gateway connection and try again.';
    }

    return error.message || 'The room request failed. Please try again.';
};

export const getRoomWebSocketUrl = () => {
    if (import.meta.env.VITE_ROOM_WS_URL) {
        return import.meta.env.VITE_ROOM_WS_URL;
    }

    const apiUrl = new URL(
        import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    );
    const protocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${protocol}//${apiUrl.host}/ws`;
};
