const fs = require("fs");
const { Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType, PageNumber, Footer, Header, LevelFormat } = require("docx");

const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: "Times New Roman", size: 26 },
        paragraph: { spacing: { line: 360, after: 240 } }
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
  numbering: {
    config: [
      {
        reference: "bullets",
        levels: [{ level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 720, hanging: 360 } } } }]
      }
    ]
  },
  sections: [{
    properties: {
      page: { width: 11906, height: 16838 },
      margin: { top: 1417, bottom: 1417, left: 1701, right: 1134, header: 720, footer: 720 }
    },
    headers: {
      default: new Header({
        children: [
          new Paragraph({
            children: [new TextRun({ text: "TRƯỜNG ĐẠI HỌC CÔNG THƯƠNG TP. HỒ CHÍ MINH - KHOA CÔNG NGHỆ THÔNG TIN", italics: true })],
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
            children: [new TextRun("Trang "), new TextRun({ children: [PageNumber.CURRENT] })],
            alignment: AlignmentType.RIGHT,
            spacing: { before: 0 }
          })
        ]
      })
    },
    children: [
      new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("CHƯƠNG 1: GIỚI THIỆU DOANH NGHIỆP")] }),
      
      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("I. GIỚI THIỆU TỔNG QUAN VỀ DOANH NGHIỆP")] }),
      
      new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("1. Tổng quan về Công ty TNHH sản xuất hàng tiêu dùng Bình Tiên (Biti's)")] }),
      new Paragraph({
        children: [new TextRun("Biti's (tên đầy đủ là Công ty TNHH Sản xuất Hàng tiêu dùng Bình Tiên) là một thương hiệu giày dép uy tín, được thành lập và đặt trụ sở tại Quận 6, Thành phố Hồ Chí Minh. Theo website chính thức, công ty mang tầm nhìn trở thành nhà sản xuất hàng tiêu dùng lớn mạnh tại khu vực Châu Á, gắn liền với sứ mệnh xây dựng một cộng đồng làm việc và học tập hạnh phúc, hiệu quả.")]
      }),
      new Paragraph({ numbering: { reference: "bullets", level: 0 }, children: [new TextRun({ text: "Tên quốc tế: ", bold: true }), new TextRun("BINH TIEN CONSUMER GOODS MANUFACTURING LIMITED COMPANY")] }),
      new Paragraph({ numbering: { reference: "bullets", level: 0 }, children: [new TextRun({ text: "Địa chỉ: ", bold: true }), new TextRun("22 Lý Chiêu Hoàng, Phường 10, Quận 6, Thành phố Hồ Chí Minh, Việt Nam")] }),
      new Paragraph({ numbering: { reference: "bullets", level: 0 }, children: [new TextRun({ text: "Mã số thuế: ", bold: true }), new TextRun("0301340497")] }),
      new Paragraph({ numbering: { reference: "bullets", level: 0 }, children: [new TextRun({ text: "Ngày hoạt động: ", bold: true }), new TextRun("20/01/1992")] }),
      new Paragraph({ numbering: { reference: "bullets", level: 0 }, children: [new TextRun({ text: "Loại hình doanh nghiệp: ", bold: true }), new TextRun("Công ty trách nhiệm hữu hạn 2 thành viên trở lên ngoài NN")] }),

      new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("2. Lịch sử hình thành và phát triển của Công ty TNHH Sản xuất Hàng tiêu dùng Bình Tiên (Biti's)")] }),
      new Paragraph({
        children: [new TextRun("Khởi nghiệp từ năm 1982 với quy mô chỉ 20 công nhân chuyên sản xuất dép cao su, Biti's ban đầu hoạt động dựa trên hai tổ hợp sản xuất nhỏ là Bình Tiên và Vạn Thành. Theo báo cáo thường niên của doanh nghiệp, đến năm 1989, Biti's tự hào trở thành đơn vị quốc doanh đầu tiên được cấp quyền xuất nhập khẩu trực tiếp. Kể từ đó, công ty không ngừng mở rộng năng lực sản xuất bằng việc ứng dụng công nghệ Đài Loan để sản xuất dép xốp EVA, đồng thời liên tiếp thành lập nhiều nhà máy và chi nhánh trên toàn quốc.")]
      }),
      new Paragraph({
        children: [new TextRun("Đặc biệt vào năm 2016, sự ra mắt của thương hiệu Biti's Hunter đã đánh dấu bước chuyển mình mạnh mẽ, giúp công ty tiếp cận thành công phân khúc khách hàng trẻ tuổi. Hiện nay, theo thông tin từ website chính thức, Biti's đang vận hành nhiều nhà máy hiện đại, tiêu biểu như Biên Hòa Amata và Bảo Tiên (Trà Vinh), đồng thời mở rộng sang cả lĩnh vực bất động sản và dịch vụ. Theo quan sát của em, quá trình vươn lên này là minh chứng rõ nét cho sự linh hoạt và nỗ lực đổi mới không ngừng của doanh nghiệp.")]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("3. Lĩnh vực kinh doanh và khách hàng của doanh nghiệp")] }),
      new Paragraph({
        children: [new TextRun("Tập trung chủ yếu vào sản xuất và kinh doanh giày dép, Biti's hiện phục vụ đa dạng từ khách hàng cá nhân đến các hệ thống bán lẻ quy mô lớn. Dựa trên số liệu từ website chính thức, doanh nghiệp đang sở hữu hơn 1.500 cửa hàng và đại lý trải dài khắp cả nước. Song song với đó, công ty cũng đẩy mạnh ứng dụng các nền tảng thương mại điện tử để tối ưu hóa mạng lưới phân phối. Nhờ vậy, sản phẩm của Biti's dễ dàng tiếp cận người tiêu dùng thông qua nhiều kênh, bao gồm cả các hệ thống siêu thị lớn như Aeon, Coopmart và Vinmart.")]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun("4. Kinh nghiệm, thành tựu và danh tiếng của doanh nghiệp")] }),
      new Paragraph({
        children: [new TextRun("Xuyên suốt hơn 40 năm hoạt động, Biti's luôn khẳng định vị thế bằng việc liên tục đổi mới thiết kế và công nghệ sản xuất. Nhờ định hướng này, sản lượng tiêu thụ của doanh nghiệp đã đạt mức hơn 20 triệu sản phẩm mỗi năm (Theo báo cáo thường niên Biti's 2023). Đáng chú ý, mạng lưới xuất khẩu của Biti's đã vươn tới hơn 40 quốc gia, bao gồm các thị trường khắt khe như Mỹ, Anh, Pháp và Trung Quốc, đồng thời trở thành đối tác gia công đáng tin cậy cho những thương hiệu toàn cầu như Clarks hay Decathlon.")]
      }),
      new Paragraph({
        children: [new TextRun("Bên cạnh sản xuất, các chiến dịch marketing của công ty cũng gặt hái nhiều thành công rực rỡ. Tiêu biểu là chiến dịch \"Đi để trở về\" (2016), giúp doanh thu dòng Biti's Hunter tăng vọt 300% chỉ trong 7 ngày đầu ra mắt (Theo báo cáo chiến dịch truyền thông). Ngoài ra, dòng Biti's Hunter Street đã nhanh chóng chiếm 7% thị phần giày thể thao Việt Nam chỉ trong vỏn vẹn 4 tháng. Việc nhiều năm liền được vinh danh là Thương hiệu Quốc gia, theo quan sát của em, chính là phần thưởng xứng đáng cho những nỗ lực làm mới và giữ vững uy tín thương hiệu của Biti's.")]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("II. CƠ CẤU TỔ CHỨC QUẢN LÝ DOANH NGHIỆP")] }),
      new Paragraph({
        children: [new TextRun("Biti's hoạt động theo mô hình công ty cổ phần. Sơ đồ tổ chức được phân cấp rõ ràng từ Hội đồng thành viên, Ban Tổng giám đốc xuống các khối nhà máy. Theo tìm hiểu của em, cấu trúc này giúp công ty điều phối công việc dễ dàng và hiệu quả hơn. Các khối nhà máy bao gồm Khối nhà máy Sài Gòn, Biên Hòa, Bảo Tiên, Long An và Khối đầu tư.")]
      }),
      new Paragraph({
        children: [new TextRun("Trong thời gian thực tập, em được phân công làm việc tại Ban Helpdesk & System thuộc Phòng Công nghệ thông tin (CNTT) – bộ phận cốt lõi chịu trách nhiệm duy trì nền tảng kỹ thuật cho toàn công ty. Dưới sự hướng dẫn của các anh chị, em không chỉ dừng lại ở các tác vụ hỗ trợ người dùng cơ bản mà còn được tiếp cận sâu hơn với quy trình quản trị hạ tầng. Cụ thể, em đã tham gia hỗ trợ xử lý sự cố phần cứng, kiểm tra kết nối mạng nội bộ và tham gia cấu hình trên môi trường Windows Server. Em cũng được hướng dẫn cách quản lý và phân quyền User, Group thông qua hệ thống Active Directory, đảm bảo tính bảo mật và sự thông suốt về dữ liệu cho các phòng ban. Trải nghiệm thực tế này đã giúp em nhận thức sâu sắc rằng, sự ổn định của hệ thống máy chủ và mạng lưới hạ tầng chính là điều kiện tiên quyết để duy trì hoạt động sản xuất, kinh doanh liên tục tại một doanh nghiệp quy mô lớn như Biti's.")]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("III. KẾT QUẢ HOẠT ĐỘNG KINH DOANH CỦA DOANH NGHIỆP")] }),
      new Paragraph({
        children: [new TextRun("Giai đoạn 2016 – 2019 đánh dấu thời kỳ tăng trưởng doanh thu đầy ấn tượng của Biti's. Khởi đầu với mức doanh thu thuần 1.291 tỷ đồng vào năm 2016, con số này đã tăng mạnh 23% để đạt 1.588 tỷ đồng (2017), và tiếp đà vươn lên mốc đỉnh 1.954 tỷ đồng vào năm 2019 (Theo báo cáo tài chính nội bộ Biti's).")]
      }),
      new Paragraph({
        children: [new TextRun("Mặc dù vậy, hệ lụy từ đại dịch COVID-19 đã gây ra không ít trở ngại cho hoạt động kinh doanh. Dựa trên số liệu tài chính năm 2020, doanh thu đã sụt giảm 14,3% xuống còn 1.673 tỷ đồng, và tiếp tục lùi sâu về mức 1.235 tỷ đồng trong năm 2021. Đáng lưu ý, lợi nhuận sau thuế năm 2021 cũng giảm mạnh 89,6% chỉ còn vỏn vẹn 8,7 tỷ đồng. Tuy nhiên, theo đánh giá cá nhân thông qua các số liệu tài chính gần đây, tình hình đã có những dấu hiệu khởi sắc rõ rệt từ năm 2022 khi mức tăng trưởng quay trở lại quỹ đạo dương. Những nỗ lực phục hồi ngoạn mục này không chỉ giúp ổn định doanh thu mà còn góp phần đưa Biti's tiếp tục góp mặt trong danh sách \"Top 100 Nơi làm việc tốt nhất Việt Nam\" năm 2024.")]
      }),

      new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun("IV. KẾT LUẬN CHƯƠNG 1")] }),
      new Paragraph({
        children: [new TextRun("Trải qua hơn 4 thập kỷ hình thành và phát triển, Biti's đã vươn mình từ một xưởng sản xuất quy mô nhỏ để trở thành một trong những thương hiệu giày dép hàng đầu tại Việt Nam. Việc không ngừng mở rộng quy mô, mạnh tay đầu tư vào công nghệ và nỗ lực đổi mới sản phẩm đã tạo bệ phóng vững chắc giúp Biti's duy trì sức cạnh tranh trên cả thị trường nội địa lẫn quốc tế.")]
      }),
      new Paragraph({
        children: [new TextRun("Hơn thế nữa, bằng việc xây dựng một hệ thống phân phối rộng khắp song song với đa dạng hóa các dòng sản phẩm, công ty đã gặt hái nhiều thành tựu danh giá như chứng nhận ISO 9001:2000 hay danh hiệu \"Thương hiệu Quốc gia Việt Nam\". Dù phải đối mặt với không ít khó khăn trong bối cảnh đại dịch COVID-19, quá trình phục hồi mạnh mẽ hiện tại chính là minh chứng cho nội lực bền bỉ của Biti's. Toàn bộ những thông tin tổng quan này sẽ đóng vai trò là nền tảng cơ sở quan trọng, hỗ trợ đắc lực cho việc phân tích sâu hơn các quy trình nghiệp vụ ở những chương tiếp theo.")]
      })
    ]
  }]
});

Packer.toBuffer(doc).then((buffer) => {
  fs.writeFileSync("Bao_cao_chuong_1_dai_hoc.docx", buffer);
  console.log("Document created successfully");
});
