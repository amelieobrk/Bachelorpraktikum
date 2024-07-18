import { AxiosInstance } from 'axios';


export interface Module {
  id: number;
  name: string;
  universityId: number;
  universityWide: boolean;
}

/**
 * Api access class to access the modules endpoints.
 */
export default class ModuleApi {
  axios: AxiosInstance;

  constructor(axios: AxiosInstance) {
    this.axios = axios;
  }

  /**
   * Get a List of all Modules and their properties
   */
  getAllModules(): Promise<Module[]> {
    return this.axios.get('/module').then(res => {
      const modules: Module[] = res.data.map((m: Module) => {
        const module: Module = {
          id: m.id,
          name: m.name,
          universityId: m.universityId,
          universityWide: m.universityWide,
        }; return module;
      });
      return modules;
    })
  }

  /**
   * Get a specific Module
   * @param id 
   */
  getModuleById(id: number): Promise<Module> {
    return this.axios.get(`/module/${id}`)
      .then(res => {
        const module: Module = {
          id: res.data.id,
          name: res.data.name,
          universityId: res.data.universityId,
          universityWide: res.data.universityWide,
        }
        return module;
      })
  }

  /**
   * Create a Module
   * @param isUniversityWide 
   * @param name 
   * @param universityId 
   */
  createModule(isUniversityWide: boolean, name: string, universityId: number): Promise<void> {
    return this.axios.post('/module', {
      isUniversityWide,
      name,
      universityId
    }).then(() => { })
  }


  /**
   * Delete a module
   * @param id 
   * @param password 
   */
  deleteModule(id: number, password: string): Promise<void> {
    return this.axios.delete(`module/${id}`, { data: { password } })
      .then(() => { })
  }

  /**
   * Updates a specific module
   * @param id 
   * @param isUniversityWide 
   * @param name 
   * @param universityId 
   */
  updateModule(id: number, isUniversityWide: boolean, name: string, universityId: number): Promise<void> {
    return this.axios.patch(`module/${id}`, {
      isUniversityWide,
      name,
      universityId
    }).then(() => { })
  }

  /**
   * Add a module to the selected major
   * @param majorId 
   * @param moduleId 
   */

  addModuleToMajor(majorId: number, moduleId: number): Promise<void> {
    return this.axios.put(`/major/${majorId}/module/${moduleId}`)
      .then(() => { })
  }

  /**
   * Remove a module from a major
   * @param majorId 
   * @param moduleId 
   */
  removeModuleFromMajor(majorId: number, moduleId: number): Promise<void> {
    return this.axios.delete(`/major/${majorId}/module/${moduleId}`)
      .then(() => { })
  }

  /**
   * Add a module to the selected section
   * @param sectionId 
   * @param moduleId 
   */
  addModuleToSection(sectionId: number, moduleId: number): Promise<void> {
    return this.axios.put(`/section/${sectionId}/module/${moduleId}`)
      .then(() => { })
  }

  /**
   * Remove module from a Section
   * @param sectionId 
   * @param moduleId 
   */
  removeModuleFromSection(sectionId: number, moduleId: number): Promise<void> {
    return this.axios.delete(`/section/${sectionId}/module/${moduleId}`)
      .then(() => { })
  }

  /**
   * Gets all Modules of a specific university
   * @param uniId 
   */
  getModuleByUniversity(uniId: number): Promise<Module[]> {
    return this.axios.get(`/university/${uniId}/module`)
      .then(res => {
        const modules: Module[] = res.data.map((m: Module) => {
          const module: Module = {
            id: m.id,
            name: m.name,
            universityId: m.universityId,
            universityWide: m.universityWide,
          }; return module;
        });
        return modules;
      })
  }

  /**
   * Gets all Modules by a specific User
   * @param userId 
   */
  getModulesByUser(userId: number): Promise<Module[]> {
    return this.axios.get(`/user/${userId}/module`)
      .then(res => {
        const modules: Module[] = res.data.map((m: Module) => {
          const module: Module = {
            id: m.id,
            name: m.name,
            universityId: m.universityId,
            universityWide: m.universityWide,
          }; return module;
        });
        return modules;
      })
  }
}