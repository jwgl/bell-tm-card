package cn.edu.bnuz.bell.tm.card.api

class UrlMappings {

    static mappings = {
        // 按用户获取信息
        "/users"(resources: 'user', includes: []) {
            "/cardReissues"(resources: 'cardReissueForm') {
                "/checkers"(controller: 'cardReissueForm', action: 'checkers', method: 'GET')
            }
        }

        // 补办学生证管理
        "/cardReissues"(resources: 'cardReissueAdmin', includes:['index', 'show']) {
            "/reviews"(resources: 'cardReissueAdmin', includes: ['patch'])
        }

        // 补办学生证订单
        "/cardReissueOrders"(resources: 'cardReissueOrder')

        "500"(view: '/error')
        "404"(view: '/notFound')
        "403"(view: '/forbidden')
        "401"(view: '/unauthorized')
    }
}
