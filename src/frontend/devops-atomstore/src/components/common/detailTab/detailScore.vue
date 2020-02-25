<template>
    <section v-bkloading="{ isLoading }" class="detail-score">
        <main class="main-swiper" v-if="detail.mediaList && detail.mediaList.length">
            <section class="detail-swiper" :style="{ width: maxTransferWidth + 'px', transform: `translateX(${swiperTransfer}px)` }">
                <template v-for="media in detail.mediaList">
                    <img :src="media.mediaUrl" v-if="media.mediaType === 'PICTURE'" :key="media.id" class="media-item">
                    <video-player v-else
                        :key="media.id"
                        class="video-player vjs-custom-skin media-item"
                        ref="videoPlayer"
                        :playsinline="true"
                        :options="getPlayOption(media)">
                    </video-player>
                </template>
            </section>
        </main>

        <i class="swiper-nav nav-left" @click="changeIndex(-1)" v-if="swiperTransfer > -maxTransferWidth + 1396"></i>
        <i class="swiper-nav nav-right" @click="changeIndex(1)" v-if="swiperTransfer < 0"></i>

        <section class="summary-tab">
            <p :class="{ 'overflow': !hasShowAll }" ref="edit">
                <mavon-editor
                    :editable="false"
                    default-open="preview"
                    :subfield="false"
                    :toolbars-flag="false"
                    :box-shadow="false"
                    :external-link="false"
                    preview-background="#fff"
                    v-model="detail.description"
                    v-show="detail.description"
                >
                </mavon-editor>
            </p>
            <span class="summary-all" @click="hasShowAll = true" v-if="isOverDes && !hasShowAll"> {{ $t('展开全部') }} </span>
            <p class="g-empty summary-empty" v-if="!detail.description"> {{ $t('发布者很懒，什么都没留下！') }} </p>
        </section>

        <section class="detail-tab">
            <h3 class="comment-title"> {{ $t('用户评分') }} </h3>
            <section class="rate-group">
                <h3 class="rate-title"><animated-integer :value="detail.avgScore" digits="1"></animated-integer><span>{{ $t('共') }}{{detail.totalNum}}{{ $t('份评分') }}</span></h3>
                <hgroup class="rate-card">
                    <h3 class="rate-info" v-for="(scoreItem, index) in detail.scoreItemList" :key="index">
                        <comment-rate :rate="scoreItem.score" :width="10" :height="11"></comment-rate>
                        <p class="rate-bar">
                            <span class="dark-gray" :style="{ flex: scoreItem.num }"></span>
                            <span class="gray" :style="{ flex: (+detail.totalNum > 0) ? detail.totalNum - scoreItem.num : 1 }"></span>
                        </p>
                        <span class="rate-sum">{{scoreItem.num}}</span>
                    </h3>
                </hgroup>
                <button class="add-common" @click="showComment = true">
                    <template v-if="(detail.userCommentInfo || {}).commentFlag"> {{ $t('修改评论') }} </template>
                    <template v-else> {{ $t('撰写评论') }} </template>
                </button>
            </section>

            <h3 class="comment-title user-comment"> {{ $t('用户评论') }} </h3>
            <hgroup v-for="(comment, index) in commentList" :key="index">
                <comment :comment="comment"></comment>
            </hgroup>
            <p class="comments-more" v-if="!isLoadEnd && commentList.length > 0" @click="getComments(true)"> {{ $t('阅读更多内容') }} </p>
            <p class="g-empty comment-empty" v-if="commentList.length <= 0"> {{ $t('空空如洗，快来评论一下吧！') }} </p>
        </section>
        <transition name="atom-fade">
            <commentDialog v-if="showComment" @freshComment="freshComment" @closeDialog="showComment = false" :name="detail.name" :code="detailCode" :id="detail.detailId" :comment-id="(detail.userCommentInfo || {}).commentId"></commentDialog>
        </transition>
    </section>
</template>

<script>
    import { mapActions, mapGetters } from 'vuex'
    import animatedInteger from '../animatedInteger'
    import commentRate from '../comment-rate'
    import comment from '../comment'
    import commentDialog from '../comment/commentDialog.vue'

    import { videoPlayer } from 'vue-video-player'
    import 'video.js/dist/video-js.css'

    export default {
        components: {
            comment,
            commentRate,
            commentDialog,
            videoPlayer,
            animatedInteger
        },

        data () {
            return {
                showComment: false,
                pageSize: 10,
                pageIndex: 1,
                isLoading: false,
                swiperTransfer: 0,
                isOverDes: false,
                hasShowAll: false,
                methodsGenerator: {
                    comment: {
                        atom: (postData) => this.requestAtomComments(postData),
                        template: (postData) => this.requestTemplateComments(postData),
                        ide: (postData) => this.requestIDEComments(postData),
                        image: (postData) => this.requestImageComments(postData),
                        service: (postData) => this.requestServiceComments(postData)
                    },
                    scoreDetail: {
                        atom: () => this.requestAtomScoreDetail(this.detailCode),
                        template: () => this.requestTemplateScoreDetail(this.detailCode),
                        ide: () => this.requestIDEScoreDetail(this.detailCode),
                        image: () => this.requestImageScoreDetail(this.detailCode),
                        service: () => this.requestServiceScoreDetail(this.detailCode)
                    }
                }
            }
        },

        computed: {
            ...mapGetters('store', { 'commentList': 'getCommentList', 'detail': 'getDetail' }),

            detailCode () {
                return this.$route.params.code
            },

            type () {
                return this.$route.params.type
            },

            maxTransferWidth () {
                const mediaList = this.detail.mediaList || []
                return mediaList.length * 395 + (mediaList.length - 1) * 37
            }
        },

        mounted () {
            this.getSummaryScore()
        },

        methods: {
            ...mapActions('store', [
                'setDetail',
                'setCommentList',
                'requestAtomComments',
                'requestAtomScoreDetail',
                'requestTemplateComments',
                'requestTemplateScoreDetail',
                'requestIDEComments',
                'requestIDEScoreDetail',
                'requestImageComments',
                'requestImageScoreDetail',
                'requestServiceComments',
                'requestServiceScoreDetail'
            ]),

            getPlayOption (media) {
                return {
                    playbackRates: [0.5, 1.0, 1.5, 2.0], // 可选的播放速度
                    autoplay: false, // 如果为true,浏览器准备好时开始回放。
                    muted: false, // 默认情况下将会消除任何音频。
                    loop: false, // 是否视频一结束就重新开始。
                    preload: 'auto', // 建议浏览器在<video>加载元素后是否应该开始下载视频数据。auto浏览器选择最佳行为,立即开始加载视频（如果浏览器支持）
                    aspectRatio: '16:9', // 将播放器置于流畅模式，并在计算播放器的动态大小时使用该值。值应该代表一个比例 - 用冒号分隔的两个数字（例如"16:9"或"4:3"）
                    fluid: false, // 当true时，Video.js player将拥有流体大小。换句话说，它将按比例缩放以适应其容器。
                    sources: [{
                        type: 'video/mp4', // 类型
                        src: media.mediaUrl // url地址
                    }],
                    poster: '', // 封面地址
                    notSupportedMessage: '此视频暂无法播放，请稍后再试', // 允许覆盖Video.js无法播放媒体源时显示的默认信息。
                    controlBar: {
                        timeDivider: true, // 当前时间和持续时间的分隔符
                        durationDisplay: true, // 显示持续时间
                        remainingTimeDisplay: false, // 是否显示剩余时间功能
                        fullscreenToggle: true // 是否显示全屏按钮
                    }
                }
            },

            changeIndex (num) {
                let newTransferDis = this.swiperTransfer + num * 432
                const maxMove = this.maxTransferWidth - 1396
                if (newTransferDis >= 0) newTransferDis = 0
                if (newTransferDis <= -maxMove) newTransferDis = -maxMove
                this.swiperTransfer = newTransferDis
            },

            getSummaryScore () {
                this.isLoading = true
                this.setDetail({ avgScore: 0 })
                Promise.all([this.getComments(), this.getScoreDetail()]).catch((err) => {
                    this.$bkMessage({ message: (err.message || err), theme: 'error' })
                }).finally(() => {
                    this.isLoading = false
                    setTimeout(() => {
                        this.isOverDes = this.$refs.edit.scrollHeight > 65
                    }, 400)
                })
            },

            getComments (isAdd = false) {
                const postData = {
                    code: this.detailCode,
                    page: this.pageIndex,
                    pageSize: this.pageSize
                }

                const getCommentsMethod = this.methodsGenerator.comment[this.type]
                return getCommentsMethod(postData).then((res) => {
                    const count = res.count || 0
                    const apiList = res.records || []
                    const commentList = isAdd ? this.commentList : []

                    apiList.forEach((comment) => {
                        commentList.push({ data: comment, children: [] })
                    })
                    this.isLoadEnd = commentList.length >= count
                    this.pageIndex++
                    this.setCommentList(commentList)
                })
            },

            getScoreDetail () {
                const getScoreDetailMethod = this.methodsGenerator.scoreDetail[this.type]
                return getScoreDetailMethod().then((res) => {
                    const itemList = [
                        { score: 5, num: 0 },
                        { score: 4, num: 0 },
                        { score: 3, num: 0 },
                        { score: 2, num: 0 },
                        { score: 1, num: 0 }
                    ]
                    const apiList = res.scoreItemList || []
                    apiList.forEach((item) => {
                        const cur = itemList.find((x) => x.score === item.score)
                        if (cur) cur.num = item.num
                    })
                    this.setDetail({
                        score: res.avgScore,
                        avgScore: res.avgScore,
                        totalNum: res.totalNum,
                        scoreItemList: itemList
                    })
                })
            },

            freshComment (comment) {
                const commentList = this.commentList
                if (this.detail.userCommentInfo && this.detail.userCommentInfo.commentFlag) {
                    // 修改
                    const cur = commentList.find(item => item.data.commentId === comment.commentId) || {}
                    const curData = cur.data || {}
                    Object.assign(curData, comment)
                } else {
                    // 新增
                    commentList.unshift({ data: comment, children: [] })
                }

                this.setDetail({
                    userCommentInfo: {
                        commentFlag: true,
                        commentId: comment.commentId
                    }
                })
                this.setCommentList(commentList)
                this.getScoreDetail().catch((err) => {
                    this.$bkMessage({ message: (err.message || err), theme: 'error' })
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '@/assets/scss/conf.scss';

    .overflow {
        max-height: 65px;
        overflow: hidden;
    }

    .detail-score {
        position: relative;
        .main-swiper {
            width: 100%;
            overflow: hidden;
        }
    }

    .detail-swiper {
        margin-top: 32px;
        display: flex;
        transition: 0.525s cubic-bezier(0.42, 0, 0.58, 1);
        .media-item {
            height: 222px;
            width: 395px;
            margin-right: 37px;
            &:last-child {
                margin-right: 0;
            }
        }
    }

    .swiper-nav {
        cursor: pointer;
        position: absolute;
        display: block;
        width: 10px;
        height: 10px;
        border-left: 3px solid $navGray;
        border-bottom: 3px solid $navGray;
        opacity: .6;
        top: 135px;
        &.nav-left {
            left: -12px;
            transform: rotate(45deg);
            &:hover {
                transform: rotate(45deg) scale(1.1);
            }
        }
        &.nav-right {
            right: -12px;
            transform: rotate(225deg);
            &:hover {
                transform: rotate(225deg) scale(1.1);
            }
        }
        &:hover {
            opacity: .9;
        }
        &:active {
            opacity: .8;
        }
    }

    .summary-tab {
        overflow: hidden;
        margin-top: 26px;
        padding-bottom: 32px;
        border-bottom: 1px solid $borderLightColor;
        /deep/ .v-note-wrapper .v-note-panel .v-note-show .v-show-content {
            padding: 0;
        }
        .summary-all {
            cursor: pointer;
            color: $primaryColor;
            font-size: 14px;
            line-height: 20px;
            margin-top: 2px;
            width: auto;
            display: block;
            text-align: center;
            position: relative;
            &::before {
                content: '';
                position: absolute;
                top: 4px;
                left: calc(50% - 50px);
                width: 6px;
                height: 6px;
                display: block;
                transform: rotate(-45deg);
                border-left: 2px solid $primaryColor;
                border-bottom: 2px solid $primaryColor;
            }
        }
    }

    .summary-empty {
        margin-top: 130px;
    }

    .detail-tab {
        margin-top: 32px;
    }

    .comment-title {
        margin-top: 32px;
        height: 23px;
        font-size: 20px;
        font-weight: 500;
        color: $fontDarkBlack;
        line-height: 23px;
    }

    .user-comment {
        margin-bottom: -13px;
        margin-top: 38px;
    }

    .comments-more {
        margin: 42px auto 0;
        font-size: 14px;
        color: $primaryColor;
        line-height: 19px;
        text-align: center;
        cursor: pointer;
    }
    
    .comment-empty {
        margin-top: 70px;
    }

    .rate-group {
        margin-top: 13px;
        height: 74px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .add-common {
            width: 120px;
            height: 40px;
            background: $white;
            border: 1px solid $buttonLightColor;
            border-radius: 4px;
            font-size: 16px;
            color: $fontDarkBlack;
            line-height: 38px;
            align-self: flex-start;
            margin-top: -36px;
            &:active {
                transform: scale(.97)
            }
        }
        .rate-title {
            margin-right: 18px;
            width: 87px;
            font-weight: normal;
            span:nth-child(1) {
                display: block;
                text-align: center;
                height: 69px;
                font-size: 52px;
                color: $fontDarkBlack;
                line-height: 69px;
            }
            span:nth-child(2) {
                height: 16px;
                font-size: 12px;
                color: $fontWeightColor;
                line-height: 16px;
                text-align: center;
                display: inline-block;
                width: 100%;
            }
        }
        .rate-card {
            flex: 1;
            .rate-info {
                display: flex;
                align-items: center;
                justify-content: flex-start;
                .rate-sum {
                    height: 15px;
                    font-size: 11px;
                    font-weight: normal;
                    color: $fontWeightColor;
                    line-height: 15px;
                }
            }
            .rate-bar {
                margin: 0 6px 0 11px;
                width: 121px;
                height: 10px;
                display: flex;
                span {
                    transition: flex 200ms
                }
                .dark-gray {
                    background: $grayBlack;
                }
                .gray {
                    background: $lighterGray;
                }
            }
        }
    }
</style>
