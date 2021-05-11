/**
 * @file config
 * @author Blueking
 */

import path from 'path'
import prodEnv from './prod.env'
import devEnv from './dev.env'

export default {
    build: {
        env: prodEnv,
        assetsRoot: path.resolve(__dirname, '../dist'),
        assetsSubDirectory: 'static',
        assetsPublicPath: '/',
        productionSourceMap: true,
        productionGzip: false,
        productionGzipExtensions: ['js', 'css'],
        bundleAnalyzerReport: process.env.npm_config_report
    },
    dev: {
        env: devEnv,
        port: '8080',
        assetsSubDirectory: 'static',
        assetsPublicPath: '/',
        proxyTable: {},
        cssSourceMap: false,
        autoOpenBrowser: false
    }
}
