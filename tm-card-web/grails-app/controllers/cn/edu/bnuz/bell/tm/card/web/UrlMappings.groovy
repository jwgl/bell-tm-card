package cn.edu.bnuz.bell.tm.card.web

class UrlMappings {

    static mappings = {
        "/students"(resources: 'student', includes: []) {
            "/reissues"(resources: 'reissueForm', includes: ['index'])
        }

        "/approvers"(resources: 'approver', 'includes': []) {
            "/reissues"(resources: 'reissueApproval', includes: ['index'])
        }

        "/reissues"(resources: 'reissue', includes: ['show'])

        "/reissueOrders"(resources: 'reissueOrder', includes: ['index']) {
            "/productionOrder"(action: 'productionOrder')
            "/distributionList"(action: 'distributionList')
            "/pictures"(action: 'pictures')
        }

        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
