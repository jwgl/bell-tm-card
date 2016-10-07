package cn.edu.bnuz.bell.tm.card.web

class UrlMappings {

    static mappings = {
        "/reissueForms"(resources: 'reissueAdmin', includes:['index', 'show']) {
            "/reviews"(resources: 'reissueReview', includes: ['show'])
        }

        "/reissueOrders"(resources: 'reissueOrder', includes: ['index']) {
            "/productionOrder"(action: 'productionOrder')
            "/distributionList"(action: 'distributionList')
            "/pictures"(action: 'pictures')
        }

        "/users"(resources: 'user') {
            "/reissueForms"(resources: 'reissueForm', includes: ['index'])
        }

        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
