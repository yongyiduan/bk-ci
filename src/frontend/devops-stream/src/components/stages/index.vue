<template>
    <section class="stages-home">
        <stage v-for="(stage, index) in stages"
            :stages="stages"
            :stage-index="index"
            :stage-num="stages.length"
            :stage="stage"
            :key="stage.id"
        ></stage>

        <stage-review-panel @approve="$emit('refresh-stages')"></stage-review-panel>
    </section>
</template>

<script>
    import stage from './stage/index'
    import stageReviewPanel from '@/components/StageReviewPanel'

    export default {
        components: {
            stage,
            stageReviewPanel
        },

        props: {
            stages: Array
        }
    }
</script>

<style lang="postcss" scoped>
    .stages-home {
        display: flex;
        align-items: flex-start;
    }
</style>
