<template>
  <div class="captcha-overlay" @click.self="$emit('close')">
    <div class="captcha-modal">
      <div class="captcha-header">
        <span>安全验证</span>
        <span class="captcha-close" @click="$emit('close')">✕</span>
      </div>
      <div class="captcha-body">
        <div class="puzzle-wrapper">
          <img :src="localImage" class="puzzle-bg" alt="背景图" />
          <img :src="puzzleImage" class="puzzle-piece" :style="puzzleStyle" alt="拼图块" />
        </div>
        <div class="slider-wrapper">
          <div class="slider-track">
            <div class="slider-progress" :style="{ width: sliderPercent + '%' }"></div>
            <div class="slider-thumb" :style="{ left: sliderPercent + '%' }" @mousedown="onDragStart" @touchstart="onDragStart">
              →
            </div>
          </div>
        </div>
        <div class="captcha-status">
          <span v-if="status === 'loading'">验证中...</span>
          <span v-else-if="status === 'success'" style="color: #67c23a">验证通过 ✓</span>
          <span v-else-if="status === 'error'" style="color: #f56c6c">
            验证失败
            <el-button type="text" style="margin-left: 8px" @click="loadCaptcha">点击重试</el-button>
          </span>
          <span v-else>请拖动滑块完成验证</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { captchaApi } from '@/api/index'

const props = defineProps({
  visible: Boolean,
  puzzleImage: String
})

const emit = defineEmits(['close', 'success'])

const status = ref('pending')
const sliderPercent = ref(0)
// 内部管理图片和 token，不受父组件 prop 影响
const localImage = ref('')
const puzzleImage = ref('')
const puzzleY = ref(0)
const localToken = ref('')
let puzzlePos = 0
// 拼图块位置：水平跟随滑块，垂直与缺口对齐
const puzzleStyle = computed(() => ({
  left: (sliderPercent.value / 100 * 280) + 'px',
  top: puzzleY.value + 'px'
}))

let isDragging = false
let startX = 0
let startLeft = 0

// 组件挂载时自动加载验证码
onMounted(() => {
  loadCaptcha()
})

// D15: 组件销毁时清理全局事件监听
onBeforeUnmount(() => {
  if (isDragging) {
    document.removeEventListener('mousemove', onDragMove)
    document.removeEventListener('mouseup', onDragEnd)
    document.removeEventListener('touchmove', onDragMoveTouch)
    document.removeEventListener('touchend', onDragEnd)
    isDragging = false
  }
})

function loadCaptcha() {
  status.value = 'loading'
  captchaApi.get()
    .then(res => {
      // 后端返回 bgImage（带缺口的背景图）和 puzzleImage（拼图块），背景图用 bgImage
      localImage.value = res.bgImage || ''
      puzzleImage.value = res.puzzleImage || ''
      puzzleY.value = res.puzzleY || 0
      localToken.value = res.token || ''
      status.value = 'pending'
    })
    .catch(() => {
      status.value = 'error'
    })
}

function onDragStart(e) {
  isDragging = true
  startX = e.type === 'mousedown' ? e.clientX : e.touches[0].clientX
  startLeft = sliderPercent.value

  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
  document.addEventListener('touchmove', onDragMoveTouch, { passive: false })
  document.addEventListener('touchend', onDragEnd)
}

function onDragMove(e) {
  if (!isDragging) return
  const currentX = e.clientX
  const diff = currentX - startX
  const trackWidth = 280 // 滑块轨道宽度
  let percent = (startLeft / 100 * trackWidth + diff) / trackWidth * 100
  percent = Math.max(0, Math.min(100, percent))
  sliderPercent.value = percent
}

function onDragMoveTouch(e) {
  if (!isDragging) return
  e.preventDefault()
  const currentX = e.touches[0].clientX
  const diff = currentX - startX
  const trackWidth = 280
  let percent = (startLeft / 100 * trackWidth + diff) / trackWidth * 100
  percent = Math.max(0, Math.min(100, percent))
  sliderPercent.value = percent
}

function onDragEnd() {
  if (!isDragging) return
  isDragging = false

  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  document.removeEventListener('touchmove', onDragMoveTouch)
  document.removeEventListener('touchend', onDragEnd)

  puzzlePos = Math.round(sliderPercent.value / 100 * 280)
  verify()
}

function verify() {
  status.value = 'loading'
  captchaApi.verify({
    token: localToken.value,
    position: Math.round(puzzlePos)
  })
    .then(res => {
      if (res && res.passToken) {
        status.value = 'success'
        // 把 passToken 传给父组件，用于登录接口
        emit('success', res.passToken)
      } else {
        status.value = 'error'
      }
    })
    .catch(() => {
      status.value = 'error'
    })
}
</script>

<style scoped>
.captcha-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}
.captcha-modal {
  width: 340px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}
.captcha-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  font-size: 16px;
  font-weight: 500;
}
.captcha-close {
  cursor: pointer;
  color: #999;
  font-size: 18px;
}
.captcha-body {
  padding: 16px;
}
.puzzle-wrapper {
  position: relative;
  width: 280px;
  height: 150px;
  margin: 0 auto;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}
.puzzle-bg {
  width: 280px;
  height: 150px;
}
.puzzle-piece {
  position: absolute;
  width: 40px;
  height: 40px;
  pointer-events: none;
  z-index: 10;
}
.slider-wrapper {
  width: 280px;
  margin: 0 auto 12px;
}
.slider-track {
  position: relative;
  height: 40px;
  background: #e8e8e8;
  border-radius: 20px;
  overflow: hidden;
}
.slider-progress {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #67c23a, #85ce61);
  border-radius: 20px;
  transition: width 0.1s;
}
.slider-thumb {
  position: absolute;
  top: 2px;
  width: 36px;
  height: 36px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  font-size: 14px;
  color: #666;
  margin-left: -18px;
}
.captcha-status {
  text-align: center;
  font-size: 14px;
  color: #666;
}
</style>