const webpack = require('webpack');
const { merge } = require('webpack-merge');
const path = require('path');
const { hashElement } = require('folder-hash');
const MergeJsonWebpackPlugin = require('merge-jsons-webpack-plugin');
const postcssRTLCSS = require('postcss-rtlcss');
const WebpackNotifierPlugin = require('webpack-notifier');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const ESLintPlugin = require('eslint-webpack-plugin');
// const BrowserSyncPlugin = require('browser-sync-webpack-plugin'); // Disabled

const environment = require('./environment');
const proxyConfig = require('./proxy.conf');

module.exports = async (config, options, targetOptions) => {
  const languagesHash = await hashElement(path.resolve(__dirname, '../src/main/webapp/i18n'), {
    algo: 'md5',
    encoding: 'hex',
    files: { include: ['*.json'] },
  });

  const isDev = config.mode === 'development';
  const isServe = targetOptions?.target === 'serve';

  // ✅ Enable linting & notifier in development
  if (isDev) {
    config.plugins.push(
      new ESLintPlugin({
        baseConfig: {
          parserOptions: {
            project: ['../tsconfig.app.json'],
          },
        },
      }),
      new WebpackNotifierPlugin({
        title: 'Med Portal',
        contentImage: path.join(__dirname, 'logo-jhipster.png'),
      })
    );
  }

  // ✅ Dev server proxy & headers
  const tls = Boolean(config.devServer && config.devServer.https);
  if (config.devServer) {
    config.devServer.proxy = proxyConfig({ tls });

    config.devServer.headers = {
      'X-Frame-Options': 'ALLOWALL',
      'Content-Security-Policy': "frame-ancestors 'self' http://localhost:3000",
    };
  }

  // ❌ DISABLED: BrowserSync to avoid AggregateError
  /*
  if (isServe || config.watch) {
    config.plugins.push(
      new BrowserSyncPlugin(
        {
          host: 'localhost',
          port: 9000,
          https: tls,
          proxy: {
            target: `http${tls ? 's' : ''}://localhost:${isServe ? '4200' : '8080'}`,
            ws: true,
            proxyOptions: {
              changeOrigin: false,
            },
          },
          socket: {
            clients: {
              heartbeatTimeout: 60000,
            },
          },
        },
        {
          reload: !isServe,
        }
      )
    );
  }
  */

  // ✅ Add production bundle analyzer
  if (config.mode === 'production') {
    const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
    config.plugins.push(
      new BundleAnalyzerPlugin({
        analyzerMode: 'static',
        openAnalyzer: false,
        reportFilename: '../stats.html',
      })
    );
  }

  // ✅ Swagger + Axios assets
  config.plugins.push(
    new CopyWebpackPlugin({
      patterns: [
        {
          context: require('swagger-ui-dist').getAbsoluteFSPath(),
          from: '*.{js,css,html,png}',
          to: 'swagger-ui/',
          globOptions: { ignore: ['**/index.html'] },
        },
        {
          from: require.resolve('axios/dist/axios.min.js'),
          to: 'swagger-ui/',
        },
        { from: './src/main/webapp/swagger-ui/', to: 'swagger-ui/' },
      ],
    })
  );

  // ✅ RTL support in postcss
  const scssRule = config.module.rules.find(x => x.test && x.test.toString().includes('scss'));
  const uses = scssRule.rules.flatMap(r => r.use || r.oneOf.flatMap(o => o.use));
  const postcssLoaderOptions = uses.filter(u => u.loader.includes('postcss-loader')).map(u => u.options);

  postcssLoaderOptions.forEach(options => {
    const generateOptions = options.postcssOptions;
    options.postcssOptions = loader => {
      const postcssOptions = generateOptions(loader);
      postcssOptions.plugins.push(postcssRTLCSS());
      return postcssOptions;
    };
    options.postcssOptions.config = false;
  });

  // ✅ Global constants and i18n merging
  config.plugins.push(
    new webpack.DefinePlugin({
      I18N_HASH: JSON.stringify(languagesHash.hash),
      __VERSION__: JSON.stringify(environment.__VERSION__),
      __DEBUG_INFO_ENABLED__: environment.__DEBUG_INFO_ENABLED__ || isDev,
      SERVER_API_URL: JSON.stringify(environment.SERVER_API_URL),
    }),
    new MergeJsonWebpackPlugin({
      output: {
        groupBy: [
          { pattern: './src/main/webapp/i18n/en/*.json', fileName: './i18n/en.json' },
          { pattern: './src/main/webapp/i18n/fa/*.json', fileName: './i18n/fa.json' },
        ],
      },
    })
  );

  // Return merged config
  return merge(config);
};
