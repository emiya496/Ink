import request from './request'

export const followApi = {
  follow: (userId: number) => request.post(`/follow/${userId}`),
  unfollow: (userId: number) => request.delete(`/follow/${userId}`),
  removeFollower: (followerId: number) => request.delete(`/follow/follower/${followerId}`),
  check: (userId: number) => request.get<any, any>(`/follow/check/${userId}`),
  getFollowers: (userId: number, page = 1, size = 20) =>
    request.get<any, any>(`/follow/${userId}/followers`, { params: { page, size } }),
  getFollowing: (userId: number, page = 1, size = 20) =>
    request.get<any, any>(`/follow/${userId}/following`, { params: { page, size } }),
}
