# 📦 Data Compression Algorithms in Java

This project implements **four powerful data compression algorithms** using Java, focusing on both **text** and **image** compression techniques.

---

## ✨ Features

### 🔤 Text Compression
- **LZW (Lempel-Ziv-Welch) Compression** (lossless)
  - Dictionary-based compression technique
  - Efficient for repeated patterns in text

- **Standard Huffman Coding** (lossless)
  - Tree-based variable-length encoding
  - Frequently occurring characters get shorter codes

- **Arithmetic Coding** (lossless)
  - Encodes entire message into a single floating-point number
  - Achieves near-optimal compression rates

### 🖼️ Image Compression
- **Vector Quantization (VQ)** (lossy)
  - Image divided into blocks (vectors)
  - Vectors mapped to a codebook to reduce image size
  - Works best for grayscale images
