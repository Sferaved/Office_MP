package com.myapp.office_mp.email

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.BodyPart
import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Multipart
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Store
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class ReadEmailTask(private val context: Context) : AsyncTask<Void, Void, List<EmailData>>() {

    override fun doInBackground(vararg voids: Void): List<EmailData> {
        return readEmails(context)
    }
    private fun readEmails(context:Context): List<EmailData> {
        val resultList = mutableListOf<EmailData>()

        try {
            // Настройте свойства для подключения
            val props = Properties()
            props.setProperty("mail.store.protocol", "imaps")

            // Создайте сессию и подключитесь к почтовому ящику
            val session: Session = Session.getDefaultInstance(props, null)
            val store: Store = session.store
//            store.connect("imap.gmail.com", "sferaved.t@gmail.com", "dgsu dduv euah cqnl")
            store.connect("imap.ukr.net", "sferved.t@ukr.net", "f7K9YvpMeeZTyyKa") //Таня
//            store.connect("imap.ukr.net", "sferved.m@ukr.net", "JMhTvEgCF9GsIyAQ") //Маня
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
                                Log.d("EmailReader_Att", "Attachment: $fileName")
                                // Проверка на файл с расширением ".imfx"
                                if (fileName.endsWith(".imfx")) {
                                    val `is`: InputStream = mimeBodyPart.inputStream
                                    val zipInputStream = ZipArchiveInputStream(`is`)
                                    var entry: ZipArchiveEntry? = zipInputStream.nextZipEntry

                                    while (entry != null) {
                                       // Проверяем, что файл имеет расширение .xml
                                            Log.d("TAG_imfx", "readEmails: ${entry.name}")

                                            // Создаем ByteArrayOutputStream для записи содержимого файла
                                            val byteArrayOutputStream = ByteArrayOutputStream()
                                            var bytesRead: Int
                                            val buffer = ByteArray(1024)

                                            // Читаем содержимое файла entry в ByteArrayOutputStream
                                            while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                                                byteArrayOutputStream.write(buffer, 0, bytesRead)
                                            }

                                            // Создаем ByteArrayInputStream на основе данных из ByteArrayOutputStream
                                            val byteArrayInputStream = ByteArrayInputStream(byteArrayOutputStream.toByteArray())

                                            // Теперь у нас есть объект типа InputStream (byteArrayInputStream), содержащий содержимое текущего файла entry
                                            // Передаем его в функцию doc1Parsing только если файл имеет тип XML
                                            if (entry.name.endsWith("doc1.xml")) {
                                                doc1Parsing(
                                                    byteArrayInputStream,
                                                    resultList,
                                                    message.subject
                                                )
                                            }
                                            if (entry.name.endsWith("doc2.xml")) { // Проверяем, что файл имеет расширение .xml
                                                doc2Parsing(
                                                    byteArrayInputStream,
                                                    resultList,
                                                    message.subject)
                                            }
                                            if (entry.name.endsWith("doclist.xml")) { // Проверяем, что файл имеет расширение .xml
                                                docListParsing(
                                                    byteArrayInputStream,
                                                    resultList,
                                                    message.subject)
                                            }
                                            // Закрываем byteArrayInputStream после обработки содержимого файла
                                            byteArrayInputStream.close()
                                   // Переходим к следующему файлу в архиве
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

    private fun doc1Parsing(
        zipInputStream: InputStream?,
        resultList: MutableList<EmailData>,
        subject: String
    ) {
        zipInputStream?.use { inputStream ->
            val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.parse(inputStream)
            // Найти и вывести значения нужных тегов
            with(doc) {
                var currentEmailData: EmailData? = null
                var docNumber = ""
                getElementsByTagName("DocNumber").takeIf { it.length > 0 }?.let {
                    docNumber = it.item(0).textContent
                    Log.d("EmailReader", "DocNumber: $docNumber")
                }

                var modificationDate = ""
                getElementsByTagName("ActionDate").takeIf { it.length > 0 }?.let {
                    modificationDate = it.item(0).textContent
                    Log.d("EmailReader", "ModificationDate: ${formatModificationDate(modificationDate)}")
                }

                var comment_code = ""
                getElementsByTagName("ActionCode").takeIf { it.length > 0 }?.let {
                    comment_code = it.item(0).textContent
                }

                var comment = ""
                when (comment_code) {
                    "1" -> comment = "Принято в базу"
                    "3" -> comment = "ОФОРМЛЕНО"
                }

                var orgName = ""
                getElementsByTagName("OrgName").takeIf { it.length > 0 }?.let {
                    orgName = it.item(0).textContent
                }

                var userName = ""
                getElementsByTagName("UserName").takeIf { it.length > 0 }?.let {
                    userName = it.item(0).textContent
                }

                var docInNum = ""
                getElementsByTagName("DocInNum").takeIf { it.length > 0 }?.let {
                    docInNum = it.item(0).textContent
                }

                var MRN = ""
                getElementsByTagName("ccd_54_02").takeIf { it.length > 0 }?.let {
                    MRN = it.item(0).textContent
                }

                Log.d("EmailReader_Cont", "doc1Parsing:MRN $MRN")
                currentEmailData = EmailData(
                    docNumber = docNumber,
                    modificationDate = formatModificationDate(modificationDate),
                    comment = comment,
                    subject = subject,
                    orgName = orgName,
                    userName = userName,
                    docInNum = docInNum,
                )

                if (!comment_code.equals("0")) {
                    addValueToResultList(resultList, currentEmailData)
                }
            }
        }
    }
    private fun doc2Parsing(
        zipInputStream: InputStream?,
        resultList: MutableList<EmailData>,
        subject: String
    ) {
        zipInputStream?.use { inputStream ->
            val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.parse(inputStream)

            // Найти и вывести значения нужных тегов
            with(doc) {
                var currentEmailData: EmailData? = null
                var docNumber = ""
                var modificationDate = ""
                var comment = ""
                var orgName = ""
                var userName = ""
                var docInNum = ""

                getElementsByTagName("MRN").takeIf { it.length > 0 }?.let {
                    docNumber = it.item(0).textContent
                }

                    getElementsByTagName("ccd_registered").takeIf { it.length > 0 }?.let {
                        modificationDate = it.item(0).textContent

                    }
                    getElementsByTagName("ccd_trn_name").takeIf { it.length > 0 }?.let {
                        comment = it.item(0).textContent
                    }
                    getElementsByTagName("ccd_01_01").takeIf { it.length > 0 }?.let {
                        comment += " -> " + it.item(0).textContent
                    }
                    getElementsByTagName("OrgName").takeIf { it.length > 0 }?.let {
                        orgName = it.item(0).textContent
                    }
                    getElementsByTagName("ccd_cl_name").takeIf { it.length > 0 }?.let {
                        userName = it.item(0).textContent
                    }

                    getElementsByTagName("MRN").takeIf { it.length > 0 }?.let {
                        docInNum = it.item(0).textContent
                    }



                currentEmailData = EmailData(
                    docNumber = docNumber,
                    modificationDate = formatModificationDate(modificationDate),
                    comment = comment,
                    subject = subject,
                    orgName = orgName,
                    userName = userName,
                    docInNum = docInNum,
                )

                addValueToResultList(resultList, currentEmailData)

            }
        }
    }
    private fun docListParsing(
            zipInputStream: InputStream?,
            resultList: MutableList<EmailData>,
            subject: String
        ) {
        zipInputStream?.use { inputStream ->
            val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.parse(inputStream)

            // Найти и вывести значения нужных тегов
            with(doc) {
                var currentEmailData: EmailData? = null
                var docNumber = ""
                var modificationDate = ""
                var comment = ""
                var orgName = ""
                var userName = ""
                var docInNum = ""

                getElementsByTagName("MRN").takeIf { it.length > 0 }?.let {
                    docNumber = it.item(0).textContent
                }
                getElementsByTagName("DocNumber").takeIf { it.length > 0 }?.let {
                    docNumber = it.item(0).textContent
                }
                getElementsByTagName("CreationDate").takeIf { it.length > 0 }?.let {
                    modificationDate = it.item(0).textContent

                }
                getElementsByTagName("Comment").takeIf { it.length > 0 }?.let {
                    comment = it.item(0).textContent
                }
                getElementsByTagName("ccd_01_01").takeIf { it.length > 0 }?.let {
                    comment += " -> " + it.item(0).textContent
                }
                getElementsByTagName("SenderName").takeIf { it.length > 0 }?.let {
                    orgName = it.item(0).textContent
                }

                currentEmailData = EmailData(
                    docNumber = docNumber,
                    modificationDate = formatModificationDate(modificationDate),
                    comment = comment,
                    subject = subject,
                    orgName = orgName,
                    userName = userName,
                    docInNum = docInNum,
                )

                addValueToResultList(resultList, currentEmailData)

            }
        }
    }


    fun sendEmailWithAttachment(attachmentFile: File) {
        try {
        val props = Properties()
        props.put("mail.smtp.host", "smtp.gmail.com")
        props.put("mail.smtp.socketFactory.port", "465")
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        props.put("mail.smtp.auth", "true")
        props.put("mail.smtp.port", "465")

//        props.put("mail.smtp.host", "smtp.ukr.net")
//        props.put("mail.smtp.socketFactory.port", "465")
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
//        props.put("mail.smtp.auth", "true")
//        props.put("mail.smtp.port", "465")

//            store.connect("imap.gmail.com", "sferaved.t@gmail.com", "dgsu dduv euah cqnl")
//            store.connect("imap.ukr.net", "sferved.t@ukr.net", "f7K9YvpMeeZTyyKa") //Таня
            try {
                val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication("sferved@gmail.com", "18And051971")
                    }
                })

                // Сеанс успешно создан, добавьте дополнительный код здесь, если это необходимо
                val message = MimeMessage(session)
                message.setFrom(InternetAddress("sferaved@gmail.com"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("andrey18051@gmail.com"))
                message.subject = "Subject of the email"

                val multipart = MimeMultipart()
                val messageBodyPart = MimeBodyPart()
                messageBodyPart.setText("Body of the email")
                multipart.addBodyPart(messageBodyPart)

                val attachmentPart = MimeBodyPart()
                val dataSource = FileDataSource(attachmentFile)
                attachmentPart.dataHandler = DataHandler(dataSource)
                attachmentPart.fileName = attachmentFile.name
                multipart.addBodyPart(attachmentPart)

                message.setContent(multipart)


                // ваш текущий код для отправки письма

                // Отправка сообщения
                Transport.send(message)

            } catch (e: Exception) {
                // Обработка ошибок при создании сеанса
                Log.e("TAG_sent", "Error creating mail session", e)
            }

            Log.d("TAG_sent+", "sendEmailWithAttachment: ")



            // Логирование успешной отправки
            Log.d("TAG_sent", "Email sent successfully")
        } catch (e: MessagingException) {
            // Логирование ошибки отправки
            Log.e("TAG_sent", "Error sending email", e)

            // Вывод дополнительной информации об ошибке в лог
            Log.e("TAG_sent", "Error message: ${e.message}")
            e.printStackTrace()
        }
    }



    private fun processNode(node: Node) {
        // Проверяем, является ли текущий узел элементом
        if (node.nodeType == Node.ELEMENT_NODE) {
            val element = node as Element
            Log.d("TAG_DOC", "processNode:Тег: ${element.nodeName}")

            // Обработка атрибутов, если они есть
            val attributes = element.attributes
            for (i in 0 until attributes.length) {
                val attr = attributes.item(i)

            }
        }

        // Рекурсивно обрабатываем дочерние узлы
        val childNodes = node.childNodes
        for (i in 0 until childNodes.length) {
            processNode(childNodes.item(i))
        }
    }
//    private fun doc2Parsing(
//            zipInputStream: ZipArchiveInputStream,
//            resultList: MutableList<EmailData>,
//            subject: String
//        ) {
//        val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
//        val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
//        val doc: Document = dBuilder.parse(zipInputStream)
//
//        // Найти и вывести значения нужных тегов
//        with(doc) {
//            var currentEmailData: EmailData? = null
//            var docNumber = ""
//            getElementsByTagName("MRN").takeIf { it.length > 0 }?.let {
//                docNumber = it.item(0).textContent
//                Log.d("EmailReader", "DocNumber: $docNumber")
//            }
//
//            var modificationDate = ""
//            getElementsByTagName("ccd_submitted").takeIf { it.length > 0 }?.let {
//                modificationDate = it.item(0).textContent
//                Log.d("EmailReader", "ModificationDate: ${formatModificationDate(modificationDate)}")
//            }
//
//            var comment_code = ""
//            getElementsByTagName("ActionCode").takeIf { it.length > 0 }?.let {
//                comment_code = it.item(0).textContent
//            }
//
//            var comment = ""
//            when (comment_code) {
//                "1" -> comment = "Принято в базу"
////                2 -> println("x is 2")
////                3, 4 -> println("x is 3 or 4")
////                in 5..10 -> println("x is between 5 and 10")
////                else -> println("x is something else")
//            }
//
//            var orgName = ""
//            getElementsByTagName("OrgName").takeIf { it.length > 0 }?.let {
//                orgName = it.item(0).textContent
//            }
//
//            var userName = ""
//            getElementsByTagName("UserName").takeIf { it.length > 0 }?.let {
//                userName = it.item(0).textContent
//            }
//
//            currentEmailData = EmailData(
//                docNumber = docNumber,
//                modificationDate = formatModificationDate(modificationDate),
//                comment = comment,
//                subject = subject,
//                orgName = orgName,
//                userName = userName,
//            )
//            addValueToResultList(resultList, currentEmailData)
//        }
//    }
//
//
//    private fun doclistParsing(
//        zipInputStream: ZipArchiveInputStream,
//        resultList: MutableList<EmailData>,
//        subject: String
//    ) {
//        val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
//        val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
//        val doc: Document = dBuilder.parse(zipInputStream)
//
//
//        // Найти и вывести значения нужных тегов
//        with(doc) {
//            var currentEmailData: EmailData? = null
//            var docNumber = ""
//            getElementsByTagName("DocNumber").takeIf { it.length > 0 }?.let {
//                docNumber = it.item(0).textContent
//                Log.d("EmailReader", "DocNumber: $docNumber")
//
//            }
//            var modificationDate = ""
//            getElementsByTagName("ModificationDate").takeIf { it.length > 0 }?.let {
//                modificationDate = it.item(0).textContent
//                Log.d("EmailReader", "ModificationDate: ${formatModificationDate(modificationDate)}")
//
//            }
//
//            getElementsByTagName("FileName").takeIf { it.length > 0 }?.let {
//                val fileNameTag: String = it.item(0).textContent
//                Log.d("EmailReader", "FileName: $fileNameTag")
//
//            }
//            var comment = ""
//            getElementsByTagName("Comment").takeIf { it.length > 0 }?.let {
//                comment = it.item(0).textContent
//                Log.d("EmailReader", "Comment: $comment")
//
//            }
//
//
//            var orgName = ""
//            getElementsByTagName("OrgName").takeIf { it.length > 0 }?.let {
//                orgName = it.item(0).textContent
//            }
//
//            var userName = ""
//            getElementsByTagName("UserName").takeIf { it.length > 0 }?.let {
//                userName = it.item(0).textContent
//            }
//
//            currentEmailData = EmailData(
//                docNumber = docNumber,
//                modificationDate = formatModificationDate(modificationDate),
//                comment = comment,
//                subject = subject,
//                orgName = orgName,
//                userName = userName,
//            )
//            if(!comment.equals("Протокол обробки електронного документа")
//                && !comment.equals("Підтвердження про отримання електронного повідомлення")
//                ) {
//                addValueToResultList(resultList, currentEmailData)
//
//            }
//
//        }
//
//    }

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
