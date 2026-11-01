import api from '../api/axios';

export const searchPlayerById = async (userId) => {
    const response = await api.get(`/player/search/${encodeURIComponent(userId)}`);
    return response.data;
};

export const sendFriendRequest = async (receiverId) => {
    const response = await api.post('/friend/send-request', { receiverId });
    return response.data;
};

export const getPendingFriendRequests = async () => {
    const response = await api.get('/friend/requests/pending');
    return response.data;
};

export const acceptFriendRequest = async (requestId) => {
    const response = await api.patch(`/friend/requests/${requestId}/accept`);
    return response.data;
};

export const rejectFriendRequest = async (requestId) => {
    const response = await api.patch(`/friend/requests/${requestId}/reject`);
    return response.data;
};

export const getFriends = async () => {
    const response = await api.get('/friend/list');
    return response.data;
};

export const removeFriend = async (friendId) => {
    const response = await api.delete(`/friend/${encodeURIComponent(friendId)}`);
    return response.data;
};
