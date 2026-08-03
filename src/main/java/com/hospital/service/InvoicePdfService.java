package com.hospital.service;

import com.hospital.entity.Invoice;
import com.hospital.entity.InvoiceItem;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.InvoiceRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service @RequiredArgsConstructor @Slf4j
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepo;

    private static final BaseColor PURPLE = new BaseColor(108, 99, 255);
    private static final BaseColor LGRAY  = new BaseColor(248, 249, 255);
    private static final Font TITLE  = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD,  BaseColor.WHITE);
    private static final Font HEAD   = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,  new BaseColor(26,26,46));
    private static final Font NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(26,26,46));
    private static final Font SMALL  = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, BaseColor.GRAY);
    private static final Font TOTAL  = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,  new BaseColor(108,99,255));

    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice inv = invoiceRepo.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice","id",invoiceId));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();
            // Header
            PdfPTable hdr = new PdfPTable(1); hdr.setWidthPercentage(100);
            PdfPCell hc = new PdfPCell(); hc.setBackgroundColor(PURPLE); hc.setPadding(20); hc.setBorder(0);
            Paragraph t = new Paragraph("🏥  MediChain HMS — INVOICE", TITLE); t.setAlignment(Element.ALIGN_CENTER);
            hc.addElement(t); hdr.addCell(hc); doc.add(hdr); doc.add(Chunk.NEWLINE);
            // Meta
            PdfPTable meta = new PdfPTable(2); meta.setWidthPercentage(100);
            addRow(meta,"Invoice #:", inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "INV-"+inv.getId());
            if (inv.getCreatedAt() != null) addRow(meta,"Date:", inv.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
            addRow(meta,"Status:", inv.getStatus());
            if (inv.getPaymentMethod() != null) addRow(meta,"Payment:", inv.getPaymentMethod());
            if (inv.getTransactionId() != null) addRow(meta,"Txn ID:", inv.getTransactionId());
            doc.add(meta); doc.add(Chunk.NEWLINE);
            // Patient
            if (inv.getPatient() != null && inv.getPatient().getUser() != null) {
                var u = inv.getPatient().getUser();
                PdfPTable pt = new PdfPTable(1); pt.setWidthPercentage(100);
                PdfPCell pc = new PdfPCell(); pc.setBackgroundColor(LGRAY); pc.setPadding(12);
                pc.addElement(new Paragraph("Patient: " + u.getFirstName()+" "+u.getLastName(), HEAD));
                pc.addElement(new Paragraph("Email: " + u.getEmail(), NORMAL));
                if (u.getPhone() != null) pc.addElement(new Paragraph("Phone: "+u.getPhone(), NORMAL));
                pt.addCell(pc); doc.add(pt); doc.add(Chunk.NEWLINE);
            }
            // Items
            PdfPTable tbl = new PdfPTable(new float[]{4,1,2,2}); tbl.setWidthPercentage(100);
            for (String h : new String[]{"Description","Qty","Unit Price","Total"}) {
                PdfPCell c = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA,10,Font.BOLD,BaseColor.WHITE)));
                c.setBackgroundColor(PURPLE); c.setPadding(8); c.setBorder(0); tbl.addCell(c);
            }
            boolean alt = false;
            if (inv.getItems() != null) for (InvoiceItem it : inv.getItems()) {
                BaseColor bg = alt ? LGRAY : BaseColor.WHITE;
                for (String v : new String[]{it.getDescription(), String.valueOf(it.getQuantity()), "₹"+it.getUnitPrice(), "₹"+it.getTotalPrice()}) {
                    PdfPCell c = new PdfPCell(new Phrase(v != null ? v : "-", NORMAL));
                    c.setBackgroundColor(bg); c.setPadding(7); c.setBorder(Rectangle.BOTTOM); tbl.addCell(c);
                }
                alt = !alt;
            }
            doc.add(tbl); doc.add(Chunk.NEWLINE);
            // Total
            PdfPTable tots = new PdfPTable(2); tots.setWidthPercentage(45); tots.setHorizontalAlignment(Element.ALIGN_RIGHT);
            addTotalRow(tots,"TOTAL:", "₹" + inv.getTotalAmount(), TOTAL);
            doc.add(tots); doc.add(Chunk.NEWLINE);
            Paragraph foot = new Paragraph("Thank you for choosing MediChain HMS | support@medichain.com", SMALL);
            foot.setAlignment(Element.ALIGN_CENTER); doc.add(foot);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF gen failed: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private void addRow(PdfPTable t, String l, String v) {
        PdfPCell lc = new PdfPCell(new Phrase(l, HEAD)); lc.setBorder(Rectangle.BOTTOM); lc.setPadding(6); t.addCell(lc);
        PdfPCell vc = new PdfPCell(new Phrase(v != null?v:"-", NORMAL)); vc.setBorder(Rectangle.BOTTOM); vc.setPadding(6); t.addCell(vc);
    }
    private void addTotalRow(PdfPTable t, String l, String v, Font f) {
        PdfPCell lc = new PdfPCell(new Phrase(l, HEAD)); lc.setBorder(0); lc.setPadding(5); t.addCell(lc);
        PdfPCell vc = new PdfPCell(new Phrase(v, f)); vc.setBorder(0); vc.setPadding(5); vc.setHorizontalAlignment(Element.ALIGN_RIGHT); t.addCell(vc);
    }
}
