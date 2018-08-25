package cn.edu.bnuz.bell.tm.card.api

class UrlMappings {

    static mappings = {
        "/students"(resources: 'student', includes: []) {
            "/reissues"(resources: 'reissueForm') {
                "/approvers"(controller: 'reissueForm', action: 'approvers', method: 'GET')
                collection {
                    "/picture"(controller: 'reissueForm', action: 'picture', method: 'GET')
                    "/notice"(controller: 'reissueForm', action: 'notice', method: 'GET')
                    "/settings"(controller: 'reissueForm', action: 'settings', method: 'GET')
                }
            }
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/reissues"(resources: 'reissueApproval', includes:['index']) {
                "/workitems"(resources: 'reissueApproval', includes: ['show', 'patch'])
            }
        }

        "/reissues"(resources: 'reissue', includes: ['show'])

        "/reissueOrders"(resources: 'reissueOrder') {
            "/productionOrder"(controller: 'reissueOrderReport', action: 'productionOrder')
            "/distributionList"(controller: 'reissueOrderReport', action: 'distributionList')
            "/pictures"(controller: 'reissueOrderReport', action: 'pictures')
            collection {
                "/unorderedForms"(action: 'unorderedForms', method: 'GET')
            }
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
        "403"(view: '/forbidden')
        "401"(view: '/unauthorized')
    }
}
