package com.myapp.office_mp.email

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.w3c.dom.Document
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.BodyPart
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.MimeBodyPart
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class ReadEmailTask : AsyncTask<Void, Void, List<EmailData>>() {

    override fun doInBackground(vararg voids: Void): List<EmailData> {
        return readEmails()
    }

    private fun readEmails(): List<EmailData> {
        val resultList = mutableListOf<EmailData>()

        try {
            // Настройте свойства для подключения
            val props = Properties()
            props.setProperty("mail.store.protocol", "imaps")

            // Создайте сессию и подключитесь к почтовому ящику
            val session: Session = Session.getDefaultInstance(props, null)
            val store: Store = session.store
//            store.connect("imap.gmail.com", "sferaved.t@gmail.com", "dgsu dduv euah cqnl")
            store.connect("imap.ukr.net", "sferved.t@ukr.net", "f7K9YvpMeeZTyyKa")
            val folders: Array<Folder> = store.defaultFolder.list()
            for (folder in folders) {
                Log.d("TAG", "readEmails: " + folder.fullName)

            }

            // Откройте папку "inbox"
            val inbox: Folder = store.getFolder("Inbox")
//            val inbox: Folder = store.getFolder("inbox")
            inbox.open(Folder.READ_ONLY)

            val messages: Array<Message> = inbox.messages

            // Обработайте сообщения
            for (message in messages) {
                // Проверка на Subject: CBMSG1
                if (message.subject != null && message.subject.contains("CBMS")) {
                    // Вывести в лог различные поля сообщения
                    Log.d("EmailReader", "Subject: ${message.subject}")
                    Log.d("EmailReader", "From: ${message.from?.contentToString()}")
                    Log.d("EmailReader", "To: ${message.getRecipients(Message.RecipientType.TO)?.contentToString()}")
                    Log.d("EmailReader", "Date: ${message.sentDate}")

                    // Вывод списка вложений
                    val multipart: Multipart = message.content as Multipart
                    for (j in 0 until multipart.count) {
                        val bodyPart: BodyPart = multipart.getBodyPart(j)
                        if (bodyPart is MimeBodyPart) {
                            val mimeBodyPart: MimeBodyPart = bodyPart
                            val fileName: String? = mimeBodyPart.fileName
                            if (fileName != null) {
                                Log.d("EmailReader", "Attachment: $fileName")
                                // Проверка на файл с расширением ".imfx"
                                if (fileName.endsWith(".imfx")) {
                                    // Получить содержимое архива
                                    val `is`: InputStream = mimeBodyPart.inputStream

                                    // Распаковка архива
                                    val zipInputStream = ZipArchiveInputStream(`is`)
                                    var entry: ZipArchiveEntry? = zipInputStream.nextZipEntry

                                    while (entry != null) {
                                        // Вывести имена всех файлов в лог
                                        Log.d("EmailReader", "   Archive Content: ${entry.name}")


                                        if (entry.name.endsWith("doc2.xml")) {
                                            doc2Parsing(zipInputStream, resultList)
                                        } else if (entry.name.endsWith("doclist.xml")) {
                                            doclistParsing(zipInputStream, resultList)
                                        }

                                        entry = zipInputStream.nextZipEntry
                                    }
                                    zipInputStream.close()
                                }
                            }
                        }
                    }

                    // Дополнительные поля можно вывести по аналогии
                    // Разделитель для удобства чтения лога
                    Log.d("EmailReader", "-----------------------------")
                }
            }

            // Закрыть папку и соединение
            inbox.close(false)
            store.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("EmailReader", "Error: $e")
        }

        return resultList
    }

    private fun doc2Parsing(zipInputStream: ZipArchiveInputStream, resultList: MutableList<EmailData>) {
        val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
        val doc: Document = dBuilder.parse(zipInputStream)

        // Найти и вывести значения нужных тегов
        with(doc) {
            var currentEmailData: EmailData? = null
            var docNumber = ""
            getElementsByTagName("MRN").takeIf { it.length > 0 }?.let {
                docNumber = it.item(0).textContent
                Log.d("EmailReader", "DocNumber: $docNumber")
            }

            var modificationDate = ""
            getElementsByTagName("ccd_submitted").takeIf { it.length > 0 }?.let {
                modificationDate = it.item(0).textContent
                Log.d("EmailReader", "ModificationDate: ${formatModificationDate(modificationDate)}")
            }

            val comment = "Завершення митного оформлення"

            currentEmailData = EmailData(
                docNumber = docNumber,
                modificationDate = formatModificationDate(modificationDate),
                comment = comment
            )
            addValueToResultList(resultList, currentEmailData)
        }
    }


    private fun doclistParsing(zipInputStream: ZipArchiveInputStream, resultList: MutableList<EmailData>) {
        val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
        val doc: Document = dBuilder.parse(zipInputStream)


        // Найти и вывести значения нужных тегов
        with(doc) {
            var currentEmailData: EmailData? = null
            var docNumber = ""
            getElementsByTagName("DocNumber").takeIf { it.length > 0 }?.let {
                docNumber = it.item(0).textContent
                Log.d("EmailReader", "DocNumber: $docNumber")

            }
            var modificationDate = ""
            getElementsByTagName("ModificationDate").takeIf { it.length > 0 }?.let {
                modificationDate = it.item(0).textContent
                Log.d("EmailReader", "ModificationDate: ${formatModificationDate(modificationDate)}")

            }

            getElementsByTagName("FileName").takeIf { it.length > 0 }?.let {
                val fileNameTag: String = it.item(0).textContent
                Log.d("EmailReader", "FileName: $fileNameTag")

            }
            var comment = ""
            getElementsByTagName("Comment").takeIf { it.length > 0 }?.let {
                comment = it.item(0).textContent
                Log.d("EmailReader", "Comment: $comment")

            }

            if(!comment.equals("Протокол обробки електронного документа")
                && !comment.equals("Підтвердження про отримання електронного повідомлення")
                ) {
                currentEmailData = EmailData(
                    docNumber = docNumber,
                    modificationDate = formatModificationDate(modificationDate),
                    comment = comment
                )
                addValueToResultList(resultList, currentEmailData)
            }

        }

    }

    private fun addValueToResultList(resultList: MutableList<EmailData>, currentEmailData: EmailData) {

        resultList.add(currentEmailData)
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatModificationDate(modificationDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss")
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
            outputFormat.format(inputFormat.parse(modificationDate)!!)
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.d("EmailReader", "Error formatting ModificationDate: ${e.message}")
            modificationDate // В случае ошибки, просто возвращаем исходную строку
        }
    }
}
