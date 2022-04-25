<template>
    <PipelineEdit
        v-bind="$attrs"
        v-on="$listeners"
    />
</template>

<script>
    import PipelineEdit from '../subpages/edit.vue'

    export default {
        name: 'ZyPipelineEdit',
        components: {
            PipelineEdit
        }
    }
</script>
