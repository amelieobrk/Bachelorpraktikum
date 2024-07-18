interface NamedEntity {
  name: string
}

/**
 * Sort function to sort by the name property of a object.
 *
 * @param a
 * @param b
 */
export const sortByName = (a: NamedEntity, b: NamedEntity) => {
  if (a.name < b.name) {
    return -1
  } else if (a.name > b.name) {
    return 1
  } else {
    return 0
  }
}

/**
 * Sort function to sort by the date property of a object.
 *
 * @param a
 * @param b
 */
export const sortByDate = (a: { date: Date }, b: { date: Date }) => {
  return a.date.getTime() - b.date.getTime();
}

/**
 * Print number with at least 2 digits. Adds a leading 0 if number is < 10
 * @param n
 */
const print2Digits = (n : number) : string => {
  if (n >= 10) {
    return String(n);
  } else {
    return `0${n}`;
  }
}

/**
 * Pretty prints date as DD.MM.YYYY
 * @param date
 */
export const prettyPrintDate = (date : Date) : string => {
  const day = date.getDate();
  const month = date.getMonth() + 1;
  const year = date.getFullYear();
  return `${print2Digits(day)}.${print2Digits(month)}.${year}`
}

/**
 * Pretty prints date time as DD.MM.YYYY HH:MM:SS
 * @param date
 */
export const prettyPrintDateTime = (date : Date) : string => {
  const hour = date.getHours();
  const minute = date.getMinutes();
  const second = date.getSeconds();
  return `${prettyPrintDate(date)} ${print2Digits(hour)}:${print2Digits(minute)}:${print2Digits(second)}`
}

/**
 * Shorten text to the given length or less. It cuts at a space.
 * @param text
 * @param length
 */
export const shortenText = (text: string, length: number = 128) : string => {
  if(text.length > length) {
    const shortenedText = text.substr(0, length);
    const clippedAtSpace = shortenedText.substr(0, shortenedText.lastIndexOf(" "));
    return clippedAtSpace + "..."
  } else {
    return text;
  }
}
