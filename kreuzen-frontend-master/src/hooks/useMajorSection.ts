import {useEffect, useState} from "react";
import api from "../api";
import {MajorSection} from "../api/major";

interface useMajorSectionProps {
  userId: number
  userMajorIds: number[]
}

interface useMajorSectionReturn {
  availableMajorSections: MajorSection[]
  userMajorSectionIds: number[]
  loading: boolean
  addMajorSection: (section : MajorSection) => void
  removeMajorSection: (section : MajorSection) => void
}

/**
 * Hook that exposes major section functionalities for a specific user.
 * @param props
 */
export const useMajorSection = (props: useMajorSectionProps) : useMajorSectionReturn => {

  const {userId, userMajorIds} = props;

  const [userMajorSectionIds, setUserMajorSectionIds] = useState<number[]>([]);
  const [availMajorSections, setAvailMajorSections] = useState<MajorSection[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Load Major Sections
  useEffect(() => {
    Promise.all([
      Promise.all(
        userMajorIds.map(major => api.major.getSectionsByMajor(major))
      ).then(sections => setAvailMajorSections(sections.flat(1))),
      Promise.all(
        userMajorIds.map(major => api.section.getSectionsByUser(userId, major))
      ).then(sections => setUserMajorSectionIds(sections.flat(1).map(s => s.id)))
    ]).then(() => setLoading(false));
  }, [userId, userMajorIds])

  const addMajorSection = (section : MajorSection) => {
    api.section.addMajorSectionToUser(userId, section.majorId, section.id).then(() => {
      setUserMajorSectionIds(sections => [
        ...sections,
        section.id
      ])
    })
  }

  const removeMajorSection = (section : MajorSection) => {
    api.section.removeMajorSectionFromUser(userId, section.majorId, section.id).then(() => {
      setUserMajorSectionIds(sections => sections.filter(s => s !== section.id))
    })
  }

  return {
    availableMajorSections: availMajorSections,
    userMajorSectionIds,
    loading,
    addMajorSection,
    removeMajorSection
  }
}