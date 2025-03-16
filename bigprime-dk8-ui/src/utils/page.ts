export namespace PageUtils {
  export const setTableHeight = (minusHeight: any) => {
    if (!minusHeight) minusHeight = 200
    const windowHeight = window.innerHeight
    return windowHeight - minusHeight
  }
}
