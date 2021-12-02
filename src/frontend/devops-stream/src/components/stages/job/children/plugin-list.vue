<script>
    import pluginComponent from '../../plugin/index'

    export default {
        functional: true,

        render (h, ctx) {
            return ctx.props.job.elements.map((plugin, pluginIndex) => {
                return h(
                    pluginComponent,
                    {
                        props: {
                            plugin,
                            key: plugin.id,
                            pluginIndex,
                            jobIndex: ctx.props.jobIndex,
                            stageIndex: ctx.props.stageIndex
                        }
                    }
                )
            })
        }
    }
</script>
