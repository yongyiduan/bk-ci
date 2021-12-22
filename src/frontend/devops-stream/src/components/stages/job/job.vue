<script>
    import jobHome from './children/job-home.vue'
    import pluginList from './children/plugin-list.vue'

    export default {
        functional: true,

        render (h, ctx) {
            return [
                h(
                    jobHome,
                    {
                        props: {
                            ...ctx.props
                        },
                        style: {
                            marginTop: '16px',
                            marginBottom: '16px'
                        }
                    }
                ),
                h(
                    pluginList,
                    {
                        props: {
                            ...ctx.props
                        }
                    }
                )
            ]
        }
    }
</script>
