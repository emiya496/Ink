import request from './request'

export const tagApi = {
  list: (type?: string) => request.get<any, any>('/tag/list', { params: { type } }),
  create: (tagName: string) => request.post<any, any>('/tag/create', { tagName }),
  hot: (limit = 20) => request.get<any, any>('/tag/hot', { params: { limit } }),
}

export const commentApi = {
  add: (contentId: number, content: string) => request.post('/comment', { contentId, content }),
  list: (contentId: number, page = 1, size = 20) =>
    request.get<any, any>(`/comment/${contentId}`, { params: { page, size } }),
  delete: (id: number) => request.delete(`/comment/${id}`),
}

export const favoriteApi = {
  add: (contentId: number) => request.post(`/favorite/${contentId}`),
  remove: (contentId: number) => request.delete(`/favorite/${contentId}`),
  list: (params: any) => request.get<any, any>('/favorite/list', { params }),
  check: (contentId: number) => request.get<any, any>(`/favorite/check/${contentId}`),
}
