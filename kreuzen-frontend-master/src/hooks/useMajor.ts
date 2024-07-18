import {useEffect, useState} from "react";
import api from "../api";
import {Major} from "../api/university";

interface useMajorProps {
  userId: number
  universityId: number | null | undefined
}

interface useMajorReturn {
  availableMajors: Major[]
  userMajorIds: number[]
  loading: boolean
  addMajor: (id: number) => void
  removeMajor: (id: number) => void
}

/**
 * Hook that exposes major functionalities for a specific user.
 * @param props
 */
export const useMajor = (props: useMajorProps) : useMajorReturn => {

  const {universityId, userId} = props;

  const [availMajors, setAvailMajors] = useState<Major[]>([]);
  const [userMajorIds, setUserMajorIds] = useState<number[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Load Majors
  useEffect(() => {
    if (universityId) {
      Promise.all([
        api.university.getMajorsByUniversityId(universityId)
          .then(majors => setAvailMajors(majors)),
        api.user.getMajorsByUserId(userId)
          .then(majors => setUserMajorIds(majors.map(m => m.id)))
      ]).then(() => setLoading(false));
    }
  }, [userId, universityId])

  const addMajor = (id:number) => {
    api.major.addMajorToUser(userId, id).then(() => {
      setUserMajorIds(m => [
        ...m,
        id
      ])
    });
  }

  const removeMajor = (id:number) => {
    api.major.removeMajorFromUser(userId, id).then(() => {
      setUserMajorIds(ids => ids.filter(i => i !== id))
    })
  }

  return {
    availableMajors: availMajors,
    userMajorIds,
    loading,
    addMajor,
    removeMajor
  }
}