const UglifyJsPlugin = require('uglifyjs-webpack-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin');
const HtmlWebpackInlineSourcePlugin = require('html-webpack-inline-source-plugin');
const packageJSON = require("./package.json");
const path = require("path");
const BUILD_PATH = path.join(__dirname, 'target', 'classes', 'META-INF', 'resources', 'webjars', packageJSON.name, packageJSON.version);

module.exports = {
	output: { path: BUILD_PATH },
	module: {
		rules: [ {
				test: /\.css$/,
				use: [ {loader: 'style-loader'}, {loader: 'css-loader'}]
			}, {
				test: /\.(gif|png|jpe?g|svg)$/i,
				use: [ {loader: 'url-loader?prefix=&limit=5000000'} ]
			}, {
				test: /\.(html)$/,
				use: [ {loader: 'html-loader'} ]
			}
		]
	},
	plugins: [
		new UglifyJsPlugin(),
		new HtmlWebpackPlugin({template: 'src/index.html', inlineSource: '.(js)$'}),
		new HtmlWebpackInlineSourcePlugin()
	]
}
