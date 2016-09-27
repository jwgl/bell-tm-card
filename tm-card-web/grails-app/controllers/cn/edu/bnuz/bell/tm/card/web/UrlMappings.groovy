package cn.edu.bnuz.bell.tm.card.web

class UrlMappings {

    static mappings = {
        "/cardReissues"(resources: 'cardReissueAdmin', includes:['index', 'show']) {
            "/reviews"(resources: 'cardReissueReview', includes: ['show'])
        }

        "/cardReissueOrders"(resources: 'cardReissueOrder', includes: ['index']) {
            "/productionOrder"(action: 'productionOrder')
            "/distributionList"(action: 'distributionList')
            "/pictures"(action: 'pictures')
        }

        "/users"(resources: 'user') {
            "/cardReissues"(resources: 'cardReissueForm', includes: ['index'])
        }

        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
