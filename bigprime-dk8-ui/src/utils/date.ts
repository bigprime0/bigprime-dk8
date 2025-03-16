import dayjs from 'dayjs'
import moment from 'moment'

export const addTimeToDate = (date: Date, hours: number, minutes: number, seconds: number) => {
  const millisecondsToAdd = hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000
  return dayjs(date).add(millisecondsToAdd)
}

export const getNowDateTime = () => {
  return dayjs().format('YYYY-MM-DD HH:mm:ss')
}

export const getNowDate = () => {
  return dayjs().format('YYYY-MM-DD')
}

export const formatDateTime = (date: any) => {
  if (date) {
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
  }
  return ''
}

export const formatDate = (date: any, fmt: string) => {
  if (date) {
    return dayjs(date).format(fmt || 'YYYY-MM-DD HH:mm:ss')
  }
  return ''
}

export const getDateYear = (date: Date) => {
  return dayjs(date).year()
}

export const getDayAndHours = (date: string) => {
  const momentObj = moment(date)
  const now = moment()
  const duration = moment.duration(now.diff(momentObj))
  if (duration.asDays() < 1) {
    if (duration.hours() < 1) {
      return duration.minutes() + 'm'
    } else {
      return duration.hours() + 'h'
    }
  } else {
    return Math.floor(duration.asDays()) + 'd '
  }
}
