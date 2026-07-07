<template>
  <div class="captcha-container" v-if="visible">
    <div class="captcha-mask" @click="close"></div>
    <div class="captcha-panel">
      <div class="captcha-header">
        <span>请完成安全验证</span>
        <span class="close-btn" @click="close">✕</span>
      </div>
      <div class="captcha-body">
        <div class="captcha-bg-wrapper" ref="bgWrapper">
          <img :src="captchaData.bgImage" class="captcha-bg" ref="bgImg" @load="onBgLoaded" />
          <div class="puzzle-slot" :style="{ top: captchaData.puzzleY + 'px', left: '0px', width: '40px', height: '40px', background: 'rgba(255,255,255,0.3)' }"></div>
        </div>
        <div class="puzzle-piece-wrapper" ref="puzzleWrapper">
          <img :src="captchaData.puzzleImage" class="puzzle-piece"
            :style="{ left: puzzlePos + 'px', top: captchaData.puzzleY + 'px', position: 'absolute' }" />
        </div>
      </div>
      <div class="captcha-slider-wrapper">
        <div class="slider-track">
          <div class="slider-fill" :style="{ width: sliderPercent + '%' }"></div>
          <div class="slider-thumb" :style="{ left: sliderPercent + '%' }"
            @mousedown="startDrag" @touchstart="startDrag"
            :class="{ dragging: isDragging }">
            →
          </div>
        </div>
      </div>
      <div class="captcha-footer">
        <span v-if="status === 'idle'" class="hint-text">拖动滑块完成拼图</span>
        <span v-if="status === 'loading'" class="hint-text loading">验证中...</span>
        <span v-if="status === 'success'" class="hint-text success">✓ 验证通过</span>
        <span v-if="status === 'error'" class="hint-text error">验证失败，请重试</span>
      </div>
    </div>
  </div>
</template>

<script>
import { captchaApi } from '../api/index.js'

export default {
  name: 'CaptchaSlider',
  props: {
    visible: { type: Boolean, default: false }
  },
  emits: ['close', 'success'],
  data() {
    return {
      captchaData: { bgImage: '', puzzleImage: '', token: '', puzzleY: 0 },
      puzzlePos: 0,
      sliderPercent: 0,
      isDragging: false,
      status: 'idle', // idle | loading | success | error
      startX: 0
    }
  },
  watch: {
    visible(v) {
      if (v) this.loadCaptcha()
    }
  },
  methods: {
    async loadCaptcha() {
      try {
        const res = await captchaApi.get()
        this.captchaData = res.data
        this.puzzlePos = 0
        this.sliderPercent = 0
        this.status = 'idle'
      } catch (e) {
        this.status = 'error'
      }
    },
    onBgLoaded() {
      // 背景图片加载后调整拼图位置
    },
    startDrag(e) {
      this.isDragging = true
      const clientX = e.clientX || e.touches[0].clientX
      this.startX = clientX - (this.sliderPercent / 100) * 250
      document.addEventListener('mousemove', this.onDrag)
      document.addEventListener('mouseup', this.stopDrag)
      document.addEventListener('touchmove', this.onDrag, { passive: true })
      document.addEventListener('touchend', this.stopDrag)
    },
    onDrag(e) {
      if (!this.isDragging) return
      const clientX = e.clientX || (e.touches && e.touches[0].clientX)
      if (!clientX) return
      let diff = clientX - this.startX
      diff = Math.max(0, Math.min(250, diff))
      this.sliderPercent = (diff / 250) * 100
      this.puzzlePos = diff
    },
    stopDrag() {
      if (!this.isDragging) return
      this.isDragging = false
      document.removeEventListener('mousemove', this.onDrag)
      document.removeEventListener('mouseup', this.stopDrag)
      document.removeEventListener('touchmove', this.onDrag)
      document.removeEventListener('touchend', this.stopDrag)
      this.verify()
    },
    async verify() {
      this.status = 'loading'
      try {
        const res = await captchaApi.verify(this.captchaData.token, Math.round(this.puzzlePos))
        this.status = 'success'
        setTimeout(() => {
          this.$emit('success', res.data.passToken)
          this.$emit('close')
        }, 500)
      } catch (e) {
        this.status = 'error'
        setTimeout(() => this.loadCaptcha(), 1000)
      }
    },
    close() {
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.captcha-container { position: fixed; top: 0; left: 0; width: 100%; height: 100%; z-index: 9999; display: flex; justify-content: center; align-items: center; }
.captcha-mask { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); }
.captcha-panel { position: relative; background: #fff; border-radius: 8px; width: 320px; box-shadow: 0 8px 30px rgba(0,0,0,0.15); overflow: hidden; }
.captcha-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; font-size: 15px; border-bottom: 1px solid #eee; }
.close-btn { cursor: pointer; color: #999; font-size: 18px; }
.captcha-body { padding: 16px; }
.captcha-bg-wrapper { position: relative; width: 280px; height: 150px; margin: 0 auto; }
.captcha-bg { width: 100%; height: 100%; display: block; border-radius: 4px; }
.puzzle-slot { position: absolute; }
.puzzle-piece-wrapper { position: relative; width: 280px; height: 0; margin: 0 auto; }
.puzzle-piece { width: 40px; height: 40px; }
.captcha-slider-wrapper { padding: 0 16px 8px; }
.slider-track { position: relative; width: 250px; height: 40px; background: #f0f0f0; border-radius: 20px; margin: 0 auto; }
.slider-fill { position: absolute; left: 0; top: 0; height: 100%; background: #7ac23c; border-radius: 20px 0 0 20px; transition: none; }
.slider-thumb { position: absolute; top: -4px; width: 48px; height: 48px; background: #fff; border: 2px solid #ddd; border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: grab; font-size: 18px; color: #666; box-shadow: 0 2px 6px rgba(0,0,0,0.15); margin-left: -24px; transition: none; user-select: none; }
.slider-thumb.dragging { cursor: grabbing; border-color: #409eff; }
.captcha-footer { text-align: center; padding: 0 16px 12px; font-size: 13px; }
.hint-text { color: #999; }
.hint-text.loading { color: #409eff; }
.hint-text.success { color: #67c23a; }
.hint-text.error { color: #f56c6c; }
</style>