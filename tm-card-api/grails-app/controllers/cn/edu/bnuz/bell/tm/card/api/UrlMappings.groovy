package cn.edu.bnuz.bell.tm.card.api

class UrlMappings {

    static mappings = {
        // 按用户获取信息
        "/users"(resources: 'user', includes: []) {
            "/reissueForms"(resources: 'reissueForm') {
                "/checkers"(controller: 'reissueForm', action: 'checkers', method: 'GET')
            }
        }

        // 补办学生证管理
        "/reissueForms"(resources: 'reissueAdmin', includes:['index', 'show']) {
            "/reviews"(resources: 'reissueAdmin', includes: ['patch'])
        }

        // 补办学生证订单
        "/reissueOrders"(resources: 'reissueOrder')

        "500"(view: '/error')
        "404"(view: '/notFound')
        "403"(view: '/forbidden')
        "401"(view: '/unauthorized')
    }
}
