// Allure 3 report configuration for the PyTest suite, read by
// `allure generate` from tox.ini.
export default {
  name: "yangtools PyTest",
  output: "./allure-report",
  // Trend data: each run appends to this file, so restoring it from the
  // previous run before tests start gives the report its trend charts.
  historyPath: "./history/history.jsonl",
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
