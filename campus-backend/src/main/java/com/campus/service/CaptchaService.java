package com.campus.service;

import com.campus.common.Result;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings("null")
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();

    private static final int BG_WIDTH = 280;
    private static final int BG_HEIGHT = 150;
    private static final int PUZZLE_SIZE = 40;
    private static final int TOLERANCE = 5;          // 容错像素
    private static final long CAPTCHA_TTL = 5;        // 5分钟有效

    public CaptchaService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成滑块验证码
     * 返回: { token, bgImage(base64), puzzleImage(base64), puzzleX(正确位置) }
     */
    public Result<?> generate() {
        // 1. 生成背景图
        BufferedImage bg = new BufferedImage(BG_WIDTH, BG_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bg.createGraphics();
        // 渐变背景
        for (int y = 0; y < BG_HEIGHT; y++) {
            int r = 100 + (y * 80 / BG_HEIGHT);
            int g = 150 + (y * 60 / BG_HEIGHT);
            int b = 200 + (y * 40 / BG_HEIGHT);
            g2d.setColor(new Color(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255)));
            g2d.fillRect(0, y, BG_WIDTH, 1);
        }
        // 随机干扰线条
        g2d.setColor(new Color(200, 200, 200, 80));
        for (int i = 0; i < 8; i++) {
            int x1 = random.nextInt(BG_WIDTH);
            int y1 = random.nextInt(BG_HEIGHT);
            int x2 = random.nextInt(BG_WIDTH);
            int y2 = random.nextInt(BG_HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }
        g2d.dispose();

        // 2. 随机抠图位置 (保证拼图块在有效范围内)
        int puzzleX = 30 + random.nextInt(BG_WIDTH - PUZZLE_SIZE - 60);
        int puzzleY = random.nextInt(BG_HEIGHT - PUZZLE_SIZE);

        // 3. 抠出拼图块
        BufferedImage puzzle = new BufferedImage(PUZZLE_SIZE, PUZZLE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = puzzle.createGraphics();
        for (int x = 0; x < PUZZLE_SIZE; x++) {
            for (int y = 0; y < PUZZLE_SIZE; y++) {
                int rgb = bg.getRGB(puzzleX + x, puzzleY + y);
                // 给拼图块加白边和半透明效果
                boolean isEdge = x == 0 || x == PUZZLE_SIZE - 1 || y == 0 || y == PUZZLE_SIZE - 1;
                if (isEdge) {
                    puzzle.setRGB(x, y, new Color(255, 255, 255, 200).getRGB());
                } else {
                    puzzle.setRGB(x, y, rgb);
                }
                // 在原图上挖空 (变暗)
                bg.setRGB(puzzleX + x, puzzleY + y, darken(rgb));
            }
        }
        pg.dispose();

        // 4. 转 Base64
        String bgBase64 = toBase64(bg, "png");
        String puzzleBase64 = toBase64(puzzle, "png");

        // 5. 存入 Redis (token → puzzleX)
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("captcha:" + token, String.valueOf(puzzleX), CAPTCHA_TTL, TimeUnit.MINUTES);

        return Result.success(java.util.Map.of(
                "token", token,
                "bgImage", "data:image/png;base64," + bgBase64,
                "puzzleImage", "data:image/png;base64," + puzzleBase64,
                "puzzleY", puzzleY
        ));
    }

    /**
     * 验证滑块验证码
     */
    public Result<?> verify(String token, int position) {
        String key = "captcha:" + token;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null) {
            return Result.error(400, "验证码已过期，请刷新");
        }
        redisTemplate.delete(key); // 一次性使用

        int correctX = Integer.parseInt(stored);
        if (Math.abs(position - correctX) <= TOLERANCE) {
            // 验证通过，生成临时凭证（5分钟有效，用于注册/重置密码）
            String passToken = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set("captcha_pass:" + passToken, "1", CAPTCHA_TTL, TimeUnit.MINUTES);
            return Result.success(java.util.Map.of("passToken", passToken));
        }
        return Result.error(400, "验证码不匹配，请重试");
    }

    /**
     * 发送验证码到邮箱（模拟：打印到控制台）
     *
     * @param email 目标邮箱
     * @param scene 场景标识，如 "forgot-password"、"register"
     */
    public void sendEmailCode(String email, String scene) {
        String code = String.format("%06d", random.nextInt(1000000));
        String key = "email_code:" + scene + ":" + email;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        System.out.println("========================================");
        System.out.println("[模拟邮件] 发送到: " + email);
        System.out.println("[模拟邮件] 验证码: " + code);
        System.out.println("[模拟邮件] 场景: " + scene);
        System.out.println("[模拟邮件] 有效期: 5分钟");
        System.out.println("========================================");
    }

    /**
     * 验证邮箱验证码
     *
     * @param email 目标邮箱
     * @param scene 场景标识，需与发送时一致
     * @param code  用户输入的验证码
     */
    public boolean verifyEmailCode(String email, String scene, String code) {
        String key = "email_code:" + scene + ":" + email;
        String stored = redisTemplate.opsForValue().get(key);
        if (stored != null && stored.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String toBase64(BufferedImage img, String format) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(img, format, bos);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("图片编码失败", e);
        }
    }

    private int darken(int rgb) {
        int r = Math.max(0, ((rgb >> 16) & 0xFF) - 60);
        int g = Math.max(0, ((rgb >> 8) & 0xFF) - 60);
        int b = Math.max(0, (rgb & 0xFF) - 60);
        return (255 << 24) | (r << 16) | (g << 8) | b;
    }
}