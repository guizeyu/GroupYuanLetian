const {defineConfig} = require('@vue/cli-service')
module.exports = defineConfig({
    transpileDependencies: true,
    //当在Gitee Page上部署时
    // publicPath: process.env.NODE_ENV === 'production' ? '/advancedwebfrontendprivate' : '/'
    //当在Docker中部署时
    publicPath: '/'
})
