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
    import { mapGetters } from 'vuex'
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
                'artifactExtensions',
                'extensionKeyMap'
            ])
        },
        created () {
            console.log(this.artifactExtensions)
        },
        methods: {
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
