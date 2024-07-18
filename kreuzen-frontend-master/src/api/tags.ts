import { AxiosInstance } from 'axios';

export interface Tag {
  id: number
  moduleId: number
  name: string
}

/**
 * Api access class to access the tag endpoints.
 */
export default class TagApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Gets a list of all tags
   *
   * @param moduleId
   */
  getTags( moduleId: number): Promise<Tag[]> {
    return this.axios.get(`/module/${moduleId}/tag`)
      .then(res => {
        const tags: Tag[] = res.data.map((t: Tag) => {
          const tag: Tag = {
            id: t.id,
            moduleId: t.moduleId,
            name: t.name,
          };
          return tag;
        });
        return tags;
      })
  }

  /**
   * Deletes a tag
   *
   * @param id
   */
  deleteTag(id: number): Promise<void> {
    return this.axios.delete(`/tag/${id}`)
      .then(() => { })
  }

  /**
   * Creates a tag
   *
   * @param name
   * @param moduleId
   */
  createTag(name: string, moduleId: number): Promise<void> {
    return this.axios.post(`/module/${moduleId}/tag`, { 
      moduleId,
      name,
    }).then(() => { })
  }

  /**
   * Gets a specific tag by the tag id
   *
   * @param id
   */
  getTagById(id: number): Promise<Tag> {
    return this.axios.get(`/tag/${id}`, { params: id })
      .then(res => {
        const tag: Tag = {
          id: res.data.id,
          moduleId: res.data.moduleId,
          name: res.data.name,
        }
        return tag;
      })
  }

  /**
   * Updates a specific tag
   *
   * @param id
   * @param newName
   */
  updateTag(id: number, newName: string): Promise<void> {
    return this.axios.patch(`/tag/${id}`, {
      newName,
    }).then(() => { })
  }
}
