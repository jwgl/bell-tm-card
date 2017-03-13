package cn.edu.bnuz.bell.card

import cn.edu.bnuz.bell.organization.Student
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.StateObject
import cn.edu.bnuz.bell.workflow.StateUserType
import cn.edu.bnuz.bell.workflow.WorkflowInstance

/**
 * 学生证补办申请表
 * @author Yang Lin
 */
class CardReissueForm implements StateObject {
    /**
     * 申请人
     */
    Student student

    /**
     * 事由
     */
    String reason

    /**
     * 第几次申请
     */
    Integer ordinal

    /**
     * 创建时间
     */
    Date dateCreated

    /**
     * 修改时间
     */
    Date dateModified

    /**
     * 提交时间
     */
    Date dateSubmitted

    /**
     * 审批人
     */
    Teacher approver

    /**
     * 审批时间
     */
    Date dateApproved

    /**
     * 状态，见CardReissureStateMachineConfiguration
     */
    State status

    /**
     * 工作流实例
     */
    WorkflowInstance workflowInstance

    static mapping = {
        comment          '补办学生证申请'
        dynamicUpdate    true
        id               generator: 'identity', comment: '补办学生证申请ID'
        student          comment: '申请人'
        reason           length: 200, comment: '事由'
        ordinal          comment: '序数'
        dateCreated      comment: '创建时间'
        dateModified     comment: '修改时间'
        dateSubmitted    comment: '提交时间'
        approver         comment: '审批人'
        dateApproved     comment: '审批时间'
        status           sqlType: 'state', type: StateUserType, comment: '状态'
        workflowInstance comment: '工作流实例'
    }

    static constraints = {
        workflowInstance nullable: true
        dateSubmitted    nullable: true
        approver         nullable: true
        dateApproved     nullable: true
        ordinal          unique: ['student']
    }

    String getWorkflowId() {
        WORKFLOW_ID
    }

    static final String WORKFLOW_ID = 'card.reissue'
    static final CONFIG_NOTICE = 'card.reissue.notice'
    static final CONFIG_MAX_COUNT = 'card.reissue.maxCount'
    static final CONFIG_NO_PICTURE = 'card.reissue.noPicture'
}
