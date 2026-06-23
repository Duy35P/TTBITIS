const fs = require("fs");
const { Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType, PageNumber, Footer, Header } = require("docx");

const doc = new Document({
  styles: {
    default: {
      document: {
        run: {
          font: "Times New Roman",
          size: 26, // 13pt
        },
        paragraph: {
          spacing: {
            line: 360, // 1.5 line spacing
            after: 240, // 1 Enter space
          }
        }
      }
    },
    paragraphStyles: [
      {
        id: "Heading1",
        name: "Heading 1",
        basedOn: "Normal",
        next: "Normal",
        quickFormat: true,
        run: { size: 28, bold: true, font: "Times New Roman" },
        paragraph: { spacing: { before: 240, after: 240 }, outlineLevel: 0, alignment: AlignmentType.CENTER }
      },
      {
        id: "Heading2",
        name: "Heading 2",
        basedOn: "Normal",
        next: "Normal",
        quickFormat: true,
        run: { size: 26, bold: true, font: "Times New Roman" },
        paragraph: { spacing: { before: 180, after: 180 }, outlineLevel: 1 }
      },
      {
        id: "Heading3",
        name: "Heading 3",
        basedOn: "Normal",
        next: "Normal",
        quickFormat: true,
        run: { size: 26, bold: true, font: "Times New Roman", italics: true },
        paragraph: { spacing: { before: 180, after: 180 }, outlineLevel: 2 }
      }
    ]
  },
  sections: [{
    properties: {
      page: {
        size: {
          width: 11906, // A4
          height: 16838
        },
        margin: {
          top: 1417,    // 2.5cm
          bottom: 1417, // 2.5cm
          left: 1701,   // 3cm
          right: 1134,  // 2cm
          header: 720,  // 1.27cm
          footer: 720   // 1.27cm
        }
      }
    },
    headers: {
      default: new Header({
        children: [
          new Paragraph({
            children: [
              new TextRun({ text: "TRƯỜNG ĐẠI HỌC CÔNG THƯƠNG TP. HỒ CHÍ MINH - KHOA CÔNG NGHỆ THÔNG TIN", italics: true })
            ],
            alignment: AlignmentType.LEFT,
            spacing: { after: 0 }
          })
        ]
      })
    },
    footers: {
      default: new Footer({
        children: [
          new Paragraph({
            children: [
              new TextRun("Trang "),
              new TextRun({ children: [PageNumber.CURRENT] })
            ],
            alignment: AlignmentType.RIGHT,
            spacing: { before: 0 }
          })
        ]
      })
    },
    children: [
      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("CHƯƠNG 2: PHÂN TÍCH NGHIỆP VỤ VÀ KIẾN TRÚC CƠ SỞ DỮ LIỆU")] }),
      
      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("2.1. Phân tích nghiệp vụ hệ thống O2O Lucky Draw")] }),
      new Paragraph({
        children: [
          new TextRun("Hệ thống O2O (Online to Offline) Lucky Draw được thiết kế nhằm mục đích tăng cường tương tác và giữ chân khách hàng thông qua các chương trình khuyến mãi quay thưởng. Hệ thống kết nối hoạt động mua sắm tại cửa hàng vật lý (Offline) với trải nghiệm quay thưởng trên nền tảng số (Online).")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun("Các nghiệp vụ cốt lõi của hệ thống bao gồm:"),
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Quản lý chiến dịch (Campaign Management): ", bold: true }),
          new TextRun("Cho phép Admin định nghĩa các chương trình khuyến mãi, thời gian diễn ra, tổng lượt dự kiến, luật chơi cơ bản (giá trị đơn hàng tối thiểu), và luật chơi nâng cao (theo phương thức thanh toán, theo SKU sản phẩm).")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Quản lý cửa hàng và tồn kho (Store & Inventory): ", bold: true }),
          new TextRun("Hệ thống quản lý thông tin các cửa hàng tham gia chiến dịch và kho giải thưởng tại từng cửa hàng. Điều này giúp kiểm soát rủi ro âm kho và phân bổ giải thưởng hợp lý theo từng khu vực.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Tích hợp POS và tính lượt (POS Integration & Turn Calculation): ", bold: true }),
          new TextRun("Thông qua API key, hệ thống POS của cửa hàng gửi thông tin hóa đơn (Invoice) lên hệ thống. Dựa vào các luật của chiến dịch, hệ thống tính toán số lượt quay thưởng tương ứng và phát sinh một token để khách hàng truy cập vào game.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Trải nghiệm khách hàng và quay thưởng (Customer Experience): ", bold: true }),
          new TextRun("Khách hàng quét mã QR trên hóa đơn để nhận lượt quay, hệ thống xác định danh tính (qua số điện thoại hoặc Zalo ID). Khách hàng sử dụng lượt để quay thưởng. Hệ thống xác định kết quả dựa trên xác suất và tồn kho giải thưởng theo thời gian thực.")
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("2.2. Phân tích kiến trúc cơ sở dữ liệu")] }),
      new Paragraph({
        children: [
          new TextRun("Để đáp ứng các yêu cầu nghiệp vụ trên, kiến trúc cơ sở dữ liệu được thiết kế một cách chuẩn hóa, bao gồm các bảng chính sau đây:")
        ]
      }),
      new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("2.2.1. Nhóm đối tượng cốt lõi")] }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng CUSTOMER: ", bold: true }),
          new TextRun("Lưu trữ thông tin khách hàng như số điện thoại, Zalo ID, tên khách hàng. Thông tin này giúp định danh khách hàng trên hệ thống khi họ tham gia quay thưởng.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng STAFF: ", bold: true }),
          new TextRun("Lưu trữ thông tin nhân viên (Admin và Store Staff) với các quyền hạn khác nhau để quản lý chiến dịch và xem báo cáo tương ứng với cửa hàng của họ.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng STORE: ", bold: true }),
          new TextRun("Lưu thông tin cửa hàng. Đây là thực thể quan trọng để liên kết hóa đơn, giải thưởng tồn kho và nhân viên với cửa hàng cụ thể.")
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("2.2.2. Nhóm chiến dịch và luật chơi")] }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng CAMPAIGN và CAMPAIGN_STORE: ", bold: true }),
          new TextRun("Quản lý thông tin tổng quan của chiến dịch (thời gian, tổng lượt) và việc phân bổ chiến dịch xuống từng cửa hàng.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng CAMPAIGN_RULE, CAMPAIGN_RULE_PAYMENT, CAMPAIGN_RULE_SKU: ", bold: true }),
          new TextRun("Thiết lập các điều kiện để tính số lượt quay thưởng. Bao gồm luật tính theo giá trị hóa đơn tối thiểu, luật thưởng thêm dựa vào phương thức thanh toán (ví dụ: thanh toán qua ví điện tử) và luật thưởng theo sản phẩm cụ thể (SKU).")
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("2.2.3. Nhóm giải thưởng và tồn kho")] }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng PRIZE: ", bold: true }),
          new TextRun("Lưu trữ danh sách các giải thưởng của một chiến dịch, thiết lập tỷ lệ trúng (xác suất) và cấu hình giới hạn số lần trúng trên mỗi khách hàng nhằm tránh gian lận.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng STORE_PRIZE_INVENTORY: ", bold: true }),
          new TextRun("Kiểm soát số lượng từng giải thưởng tại mỗi cửa hàng, có ràng buộc (Constraint) chống âm kho ở mức Database nhằm đảm bảo tính nhất quán của dữ liệu ngay cả khi có nhiều giao dịch đồng thời.")
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("2.2.4. Nhóm giao dịch và trò chơi")] }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng INVOICE và STORE_POS_KEY: ", bold: true }),
          new TextRun("Nhận dữ liệu hóa đơn từ POS một cách bảo mật qua API key. Hóa đơn được lưu trữ chi tiết (tổng tiền, sản phẩm, phương thức thanh toán) để phục vụ việc tính lượt chơi.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng GAME_ACCESS_TOKEN: ", bold: true }),
          new TextRun("Mỗi hóa đơn hợp lệ sẽ sinh ra một token truy cập duy nhất có thời hạn. Token này thường được chuyển thành QR code để khách quét và nhận lượt.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảng CUSTOMER_TURN và TURN_TRANSACTION: ", bold: true }),
          new TextRun("Quản lý ví lượt quay của khách hàng và ghi nhận lịch sử các lần quay thưởng (bao gồm việc sử dụng token nào, trúng giải thưởng nào, vào thời điểm nào).")
        ]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("2.3. Đánh giá ưu điểm của kiến trúc hiện tại")] }),
      new Paragraph({
        children: [
          new TextRun("Kiến trúc cơ sở dữ liệu trên mang lại các lợi ích đáng kể:")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Khả năng mở rộng (Scalability): ", bold: true }),
          new TextRun("Việc tách biệt các luật tính thưởng thành các bảng riêng giúp hệ thống dễ dàng thêm các loại luật khuyến mãi mới trong tương lai mà không ảnh hưởng đến cấu trúc lõi.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Tính toàn vẹn dữ liệu (Data Integrity): ", bold: true }),
          new TextRun("Hệ thống sử dụng triệt để các ràng buộc mức cơ sở dữ liệu (Check constraints, Unique constraints) để chống âm kho, chống nhập liệu sai trạng thái, đảm bảo sự nhất quán cho các nghiệp vụ thanh toán.")
        ]
      }),
      new Paragraph({
        children: [
          new TextRun({ text: "Bảo mật linh hoạt: ", bold: true }),
          new TextRun("Việc tách biệt cơ chế xác thực cửa hàng qua POS Key thay vì dùng chung tài khoản Staff giúp an toàn hơn khi tích hợp với các đối tác thứ ba.")
        ]
      })
    ]
  }]
});

Packer.toBuffer(doc).then((buffer) => {
  fs.writeFileSync("Bao_cao_chuong_2.docx", buffer);
  console.log("Document created successfully");
});
