// https://github.com/webpack-contrib/copy-webpack-plugin/issues/59
var fs = require('fs');
var gracefulFs = require('graceful-fs');
gracefulFs.gracefulify(fs);


const webpack = require("webpack");
const BrowserSyncPlugin = require('browser-sync-webpack-plugin');
const path = require("path");

const dist = path.resolve(__dirname, "build/dist");

// TODO: hot-reload without extraneous node.js http proxy

module.exports = {
    entry: {
        main: "main"
    },
    output: {
        pathinfo: true,
        filename: "[name].bundle.js",
        path: dist,
        publicPath: ""
    },
    watch: true,
    module: {
        rules: [{
            test: /\.css$/,
            use: [
                'style-loader',
                'css-loader'
            ]
        }]
    },
    resolve: {
        modules: [
            path.resolve(__dirname, "build/node_modules/"),
            path.resolve(__dirname, "src/main/web/")
        ]
    },
    devtool: 'cheap-source-map',
    plugins: [
//        new webpack.optimize.CommonsChunkPlugin({
//            name: 'vendor',
//            filename: 'vendor.bundle.js'
//        }),
//        new BrowserSyncPlugin({
//            host: 'localhost',
//            port: 8080,
//            server: {
//                baseDir: ['./build/dist']
//            }
//        })
    ]
};
