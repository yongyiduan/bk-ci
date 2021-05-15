<template>
    <article class="notifications-home">
        <aside class="aside-nav">
            <h3 class="nav-title">
                <i class="bk-icon icon-arrows-left" @click="backHome"></i>
                Notifications
            </h3>
            <ul>
                <li v-for="nav in navList"
                    :key="nav.name"
                    :class="{ 'nav-item': true, active: curNav.name === nav.name }"
                ><icon :name="nav.icon" size="18"></icon>{{ nav.label }}</li>
            </ul>
        </aside>

        <main class="notifications-main">
            <bk-button class="notifications-button">Mark all as read</bk-button>
            <bk-collapse>
                <bk-collapse-item name="1">
                    2021-05-01
                    <div slot="content" class="f13">Merge requests[!157] opened by fayewang trigger successed (6/6) see more</div>
                </bk-collapse-item>
                <bk-collapse-item name="2">
                    2021-05-02
                    <div slot="content" class="f13">
                        Merge requests[!157] opened by fayewang trigger successed (6/6) see more
                    </div>
                </bk-collapse-item>
                <bk-collapse-item name="3">
                    2021-05-03
                    <div slot="content" class="f13">
                        Merge requests[!157] opened by fayewang trigger successed (6/6) see more
                    </div>
                </bk-collapse-item>
                <bk-collapse-item name="4">
                    2021-05-04
                    <div slot="content" class="f13">
                        Merge requests[!157] opened by fayewang trigger successed (6/6) see more
                    </div>
                </bk-collapse-item>
            </bk-collapse>

            <bk-pagination small
                :current.sync="compactPaging.current"
                :count.sync="compactPaging.count"
                :limit="compactPaging.limit"
                :show-limit="false"
                class="notify-paging"
            />
        </main>
    </article>
</template>

<script>
    export default {
        data () {
            return {
                navList: [
                    { label: 'Notifications', name: 'notifications', icon: 'notify' }
                ],
                compactPaging: {
                    limit: 10,
                    current: 1,
                    count: 100
                },
                curNav: { name: 'notifications' }
            }
        },

        watch: {
            '$route.name': {
                handler (name) {
                    this.initRoute(name)
                },
                immediate: true
            }
        },

        methods: {
            initRoute (name) {
                let settingIndex = 0
                switch (name) {
                    case 'basicSetting':
                        settingIndex = 0
                        break
                    case 'credentialList':
                        settingIndex = 1
                        break
                    case 'agentPools':
                    case 'addAgent':
                        settingIndex = 2
                        break
                }
                this.curSetting = this.settingList[settingIndex]
            },

            goToPage ({ name }) {
                this.$router.push({ name })
            },

            backHome () {
                this.goToPage({ name: 'buildList', params: this.$route.params })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .notifications-home {
        display: flex;
        flex-direction: row;
        background: #f5f6fa;
        .notifications-main {
            flex: 1;
            background: #fff;
            margin: 25px;
            padding: 25px;
            over-flow: auto;
            .notifications-button {
                margin-bottom: 20px;
            }
        }
    }
    .notify-paging {
        margin: 12px 0 0;
        display: flex;
        align-items: center;
        justify-content: center;
        /deep/ span {
            outline: none;
            margin-left: 0;
        }
    }
</style>
