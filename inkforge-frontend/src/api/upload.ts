import request from './request'

export const uploadApi = {
  image: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return request.post<any, any>('/upload/image', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
