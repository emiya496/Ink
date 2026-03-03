import request from './request'

export const contentApi = {
  create: (data: any) => request.post<any, any>('/content/create', data),
  list: (params: any) => request.get<any, any>('/content/list', { params }),
  detail: (id: number) => request.get<any, any>(`/content/${id}`),
  update: (id: number, data: any) => request.put(`/content/${id}`, data),
  delete: (id: number) => request.delete(`/content/${id}`),
  my: (params: any) => request.get<any, any>('/content/my', { params }),
  myDrafts: (params: any) => request.get<any, any>('/content/my/drafts', { params }),
  like: (id: number) => request.post(`/content/${id}/like`),
  rank: (params: { type?: string; rankType?: string }) =>
    request.get<any, any>('/content/rank', { params }),
  hotBanner: (params: { type?: string }) =>
    request.get<any, any>('/content/hot-banner', { params }),
}

export const chapterApi = {
  add: (data: any) => request.post('/chapter', data),
  list: (contentId: number) => request.get<any, any>(`/chapter/${contentId}`),
  update: (id: number, data: any) => request.put(`/chapter/${id}`, data),
  delete: (id: number) => request.delete(`/chapter/${id}`),
}
