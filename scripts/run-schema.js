/**
 * Script chạy Database_NV.sql lên Neon qua kết nối WebSocket (không cần psql)
 * Cài dependency: npm install @neondatabase/serverless ws
 * Chạy: node scripts/run-schema.js
 */

import { neon } from '@neondatabase/serverless';
import { readFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __dirname = dirname(fileURLToPath(import.meta.url));

// Lấy từ .env hoặc truyền trực tiếp
const CONNECTION_STRING =
  process.env.DATABASE_URL ||
  'postgresql://neondb_owner:npg_AUvzmtRS1qO3@ep-twilight-wildflower-az7014s9-pooler.c-3.ap-southeast-1.aws.neon.tech/shopping_agent?sslmode=require&channel_binding=require';

async function runSchema() {
  console.log('📡 Kết nối Neon...');
  const sql = neon(CONNECTION_STRING);

  const schemaPath = join(__dirname, '..', 'Database_NV.sql');
  const schemaSQL = readFileSync(schemaPath, 'utf8');
  console.log(`📄 Đọc schema từ: ${schemaPath}`);

  // Tách các câu lệnh SQL (tách theo dấu ; nhưng giữ nguyên block BEGIN/COMMIT)
  // Neon HTTP API không hỗ trợ multi-statement trực tiếp → chạy toàn bộ như 1 block
  try {
    console.log('🚀 Chạy schema...');
    // Dùng transaction() để xử lý nhiều câu lệnh
    await sql.transaction(statements => {
      // Tách từng statement hợp lệ (bỏ comment và dòng trống)
      const stmts = schemaSQL
        .replace(/--[^\n]*/g, '')          // xóa comment dòng --
        .split(';')
        .map(s => s.trim())
        .filter(s => s.length > 0 && s !== 'BEGIN' && s !== 'COMMIT');

      return stmts.map(s => sql.unsafe(s));
    });
    console.log('✅ Schema đã được tạo thành công trên Neon!');
  } catch (err) {
    // Lỗi "already exists" là bình thường nếu chạy lần 2
    if (err.message?.includes('already exists')) {
      console.warn('⚠️  Một số bảng đã tồn tại (bình thường nếu chạy lần 2):', err.message);
    } else {
      console.error('❌ Lỗi:', err.message);
      process.exit(1);
    }
  }
}

runSchema();
