<template>
    <section class="stages-home">
        <stage v-for="(stage, index) in stages"
            :stage-index="index"
            :stage-num="stages.length"
            :stage="stage"
            :key="stage.id"
        ></stage>
    </section>
</template>

<script>
    import stage from './stage/index'

    export default {
        components: {
            stage
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
