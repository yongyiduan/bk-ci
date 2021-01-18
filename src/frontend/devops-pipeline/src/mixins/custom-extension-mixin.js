import { mapGetters, mapActions } from 'vuex'
import { isAbsoluteURL, urlJoin } from '@/utils/util'
export default {
    data () {
        return {
            extensions: []
        }
    },
    computed: {
        ...mapGetters([
            'artifactHooks',
            'extensionTabsHooks',
            'extensionExecuteDetailTabsHooks',
            'hookKeyMap'
        ]),
        hookIds () {
            return this.hooks.map(hook => hook.itemId).join(',')
        },
        hasHookIds () {
            return !!this.hookIds
        },
        hasExts () {
            return Array.isArray(this.extensions) && this.extensions.length > 0
        }
    },
    watch: {
        hookIds: {
            handler: function (hooksIds) {
                console.log(hooksIds)
                this.fetchExt(hooksIds)
            },
            immediate: true
        }
    },
    methods: {
        ...mapActions([
            'fetchExtensionByHookId'
        ]),
        getExtTooltip (ext) {
            return ext.props && ext.props.tooltip ? ext.props.tooltip : ext.tooltip
        },
        getExtIconUrl (ext) {
            return ext.props && ext.props.iconUrl ? ext.props.iconUrl : ext.iconUrl
        },
        async fetchExt (itemIds) {
            console.log('itemIds', itemIds)
            try {
                const res = await this.fetchExtensionByHookId({
                    projectCode: this.$route.params.projectId,
                    itemIds
                })
                let extensions = []
                console.log(res.data, 'res.data')
                this.extensionMap = res.data.reduce((extensionMap, ext) => {
                    const extServiceList = ext.extServiceList.map(item => ({
                        ...this.hookKeyMap[ext.itemId],
                        ...item
                    }))
                    extensionMap[ext.itemId] = [
                        ...extServiceList
                    ]

                    extensions = [
                        ...extensions,
                        ...extServiceList
                    ]

                    return extensionMap
                }, {})
                this.extensions = extensions
            } catch (error) {
                console.log(error)
            }
        },
        getResUrl (url, baseURL) {
            return isAbsoluteURL(url) ? url : urlJoin(baseURL, 'static', url)
        }
    }

}
