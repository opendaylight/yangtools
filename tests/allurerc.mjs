// Allure 3 report configuration for the PyTest suite, read by
// `allure generate` from tox.ini.
export default {
  name: "yangtools PyTest",
  output: "./allure-report",
  // Trend data: each run appends to this file, so restoring it from the
  // previous run before tests start gives the report its trend charts.
  // CI points ALLURE_HISTORY_PATH elsewhere when it could not restore the
  // earlier history, so that a fresh file is not published in its place.
  historyPath: process.env.ALLURE_HISTORY_PATH ?? "./history/history.jsonl",
  // Keep the newest 100 runs, so the file and the report embedding its
  // trend data do not grow without bound.
  historyLimit: 100,
  plugins: {
    awesome: {
      options: {
        // One self-contained index.html, so it can be served as is from
        // the log server or opened locally after download.
        singleFile: true,
      },
    },
  },
};
