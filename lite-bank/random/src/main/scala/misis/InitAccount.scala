package misis

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.sksamuel.elastic4s.ElasticClient
import misis.model.AccountUpdate
import io.circe.generic.auto._

class InitAccount(elastic: ElasticClient)(implicit val system: ActorSystem) extends WithKafka {

    override def group: String = "random"

    implicit val commandTopicName: TopicName[AccountUpdate] = simpleTopicName[AccountUpdate]

    Source(1 to 3)
        .map(accountId => AccountUpdate(accountId = accountId, value = 1000, category = None, tags = None))
        .map(command => produceCommand(command))
        .to(Sink.ignore)
        .run()


    val rand = new scala.util.Random

    val categories = List("Перевод", "Зачисление", "Списание", "Комиссия", "Кэшбек")

    val tags = List("Инвестиции", "Экономика", "Деньги", "Фондовый", "рынок", "Биржа", "Политика", "Криптовалюта",
        "Рубль", "Акции", "Биткоины", "Кризис", "Банк", "Доллары", "Инвестиции", "в", "акции", "Бизнес", "Инфляция",
        "Валюта", "Санкции", "Россия", "Налоги", "Новости", "Облигации", "Кредит", "США", "Юмор", "Личный", "опыт",
        "Картинка", "с", "текстом", "Зарплата", "Финансовая", "грамотность", "Рост", "цен", "Негатив", "Мат", "Курс",
        "доллара", "NFT", "Сбербанк", "Опыт", "Малый", "бизнес", "Нефть", "Инвестировать", "просто", "Газпром", "Пенсия",
        "Саморазвитие", "YouTube", "Банкротство", "Долг", "Ипотека", "Евросоюз", "Газ", "Война", "Цены", "Тинькофф",
        "банк", "Мемы", "Трейдинг", "Недвижимость", "Работа", "Украина", "Статистика", "Европа", "Скриншот", "Дивиденды",
        "Центральный", "банк", "РФ", "Бюджет", "Доход", "Мошенничество", "Психология", "Профессия", "Заработок",
        "Торговля", "Книги", "Мотивация", "Накопления", "Китай", "Золото", "Технологии", "Экономический", "кризис",
        "Ранняя", "пенсия", "Экономия", "История", "Отношения", "Доллар", "Вопрос", "Право", "Брокер", "Жизнь", "Обман",
        "Вклад", "Помощь", "Московская", "биржа", "Курс", "валют", "Аналитика", "Семья", "Рынок", "Сбережения")


    Source(0 to 1000)
        .map(_ =>
            AccountUpdate(
                accountId = rand.nextInt(3) + 1,
                value = rand.nextInt(1000) - 500,
                category = Some(categories(rand.nextInt(categories.size))),
                tags = Some((0 to rand.nextInt(5))
                    .map(_ => tags(rand.nextInt(tags.size))))
            )
        )
        .map(command => produceCommand(command))
        .to(Sink.ignore)
        .run()
}
