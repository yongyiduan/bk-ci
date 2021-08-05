<template>
    <section class="review-show">
        <quality-line :stage-control="stageControl"></quality-line>
        <review-describe :desc="stageControl.reviewDesc"></review-describe>
        <review-flow-show :review-groups="stageControl.reviewGroups"></review-flow-show>
        <params-show :params="stageControl.reviewParams"></params-show>
    </section>
</template>

<script>
    import ParamsShow from './components/params/show'
    import ReviewFlowShow from './components/reviewFlow/show'
    import ReviewDescribe from './components/describe'
    import QualityLine from './components/quality-line'

    export default {
        components: {
            ParamsShow,
            ReviewFlowShow,
            ReviewDescribe,
            QualityLine
        },

        props: {
            stageControl: Object
        }
    }
</script>
