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
            <bk-button class="notifications-button" @click="readAll">Mark all as read</bk-button>
            <bk-collapse v-bkloading="{ isLoading }">
                <bk-collapse-item :name="index" v-for="(notification, index) in notificationList" :key="index">
                    {{ notification.createTime }}
                    <div slot="content" class="f13">{{ notification.content.commitMsg }}</div>
                </bk-collapse-item>
            </bk-collapse>

            <bk-pagination small
                :current.sync="compactPaging.current"
                :count.sync="compactPaging.count"
                :limit="compactPaging.limit"
                :show-limit="false"
                @change="pageChange"
                class="notify-paging"
            />
        </main>
    </article>
</template>

<script>
    import { notifications } from '@/http'

    export default {
        data () {
            return {
                navList: [
                    { label: 'Notifications', name: 'notifications', icon: 'notify' }
                ],
                compactPaging: {
                    limit: 10,
                    current: 1,
                    count: 0
                },
                curNav: { name: 'notifications' },
                notificationList: [],
                isLoading: false
            }
        },

        created () {
            this.initData()
        },

        methods: {
            initData () {
                this.isLoading = true
                notifications.getUserUnreadMessages(this.compactPaging.current).then((res) => {
                    this.notificationList = res.records
                    this.compactPaging.count = res.count
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            readAll () {
                notifications.readAllMessages().then(() => {
                    this.initData()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            pageChange (page) {
                this.compactPaging.current = page
                this.initData()
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
