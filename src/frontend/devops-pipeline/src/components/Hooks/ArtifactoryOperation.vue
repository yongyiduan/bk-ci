<template>
    <ul class="artifactory-operation-hooks">
        <li v-for="hook in artifactExtensions" :key="hook.key" @click="hookAction(hook)">
            <template v-if="hook.tooltip && hook.tooltip.value">
                <img v-bk-tooltips="hook.tooltip.value" v-bind="hook.icon" :src="getResUrl(hook.icon.url, hook.appKey)" />
            </template>
            <template v-else>
                <img v-bind="hook.icon" :src="getResUrl(hook.icon.url, hook.appKey)" />
            </template>
        </li>
    </ul>
</template>

<script>
    import { mapGetters, mapActions } from 'vuex'
    import { isAbsoluteURL, urlJoin } from '@/utils/util'

    export default {
        name: 'artifactory-operation-hooks',
        props: {
            artifact: {
                type: Object,
                default: () => ({})
            }
        },
        computed: {
            ...mapGetters([
                'artifactHooks'
            ]),
            hookIds () {
                return this.artifactHooks.map(hook => hook.itemId).join(',')
            }
        },
        created () {
            console.log(this.artifactHooks)
            this.fetchExt()
        },
        methods: {
            ...mapActions([
                'fetchExtensionByHookId'
            ]),
            hookAction (hook) {
                this.$hookTrigger({
                    ...hook,
                    target: {
                        ...hook.target,
                        data: {
                            ...hook.target.data,
                            artifact: this.artifact
                        }
                    }
                })
            },
            async fetchExt () {
                try {
                    const res = await this.fetchExtensionByHookId({
                        projectCode: this.$route.params.projectId,
                        itemIds: this.hookIds
                    })
                    console.log(res)
                } catch (error) {
                    console.log(error)
                }
            },
            getResUrl (url, appKey) {
                console.log(url, appKey, 'geturl')
                return isAbsoluteURL(url) ? url : urlJoin(this.extensionKeyMap[appKey].baseURL, url)
            }
        }
    }
</script>

<style lang="scss">
    .artifactory-operation-hooks {
        display: inline-flex;
        align-items: center;
    }
</style>
