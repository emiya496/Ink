mysqldump: [Warning] Using a password on the command line interface can be insecure.
-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: inkforge
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai_log`
--

DROP TABLE IF EXISTS `ai_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `function_type` varchar(50) NOT NULL COMMENT '功能类型: generate/summary/polish/keywords/sentiment/style/qa',
  `model_name` varchar(100) NOT NULL COMMENT '使用的模型名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_function_type` (`function_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI调用日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_log`
--

LOCK TABLES `ai_log` WRITE;
/*!40000 ALTER TABLE `ai_log` DISABLE KEYS */;
INSERT INTO `ai_log` VALUES (1,1,'generate','deepseek-chat','2026-03-09 02:37:37'),(2,1,'polish','deepseek-chat','2026-03-09 02:38:09'),(3,1,'summary','deepseek-chat','2026-03-09 03:09:14');
/*!40000 ALTER TABLE `ai_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chapter`
--

DROP TABLE IF EXISTS `chapter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chapter` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_id` bigint NOT NULL COMMENT '所属内容ID',
  `chapter_title` varchar(200) DEFAULT NULL COMMENT '章节标题',
  `chapter_order` int NOT NULL COMMENT '章节序号',
  `chapter_content` longtext COMMENT '章节内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_order` (`content_id`,`chapter_order`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='章节表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chapter`
--

LOCK TABLES `chapter` WRITE;
/*!40000 ALTER TABLE `chapter` DISABLE KEYS */;
INSERT INTO `chapter` VALUES (1,5,'1',1,'<p><br></p>','2026-03-09 16:31:43'),(2,5,'2',2,'','2026-03-09 16:31:43'),(3,5,'3',3,'','2026-03-09 16:31:43'),(4,5,'4',4,'','2026-03-09 16:31:43'),(5,6,'1',1,'<p>1</p>','2026-03-09 16:32:57'),(6,6,'2',2,'<p>2</p>','2026-03-09 16:33:01'),(7,9,'1',1,'<p>1</p>','2026-03-09 16:41:16'),(8,9,'2',2,'<p>2</p>','2026-03-09 16:41:16');
/*!40000 ALTER TABLE `chapter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `user_id` bigint NOT NULL COMMENT '评论者ID',
  `content` text NOT NULL COMMENT '评论内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (1,3,2,'111','2026-03-09 16:45:16');
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content`
--

DROP TABLE IF EXISTS `content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '作者ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` longtext COMMENT '正文(非小说用)',
  `type` enum('小说','散文','诗词','随笔','名人名言','杂谈') NOT NULL COMMENT '类型',
  `status` enum('正常','审核中','下架','草稿') NOT NULL DEFAULT '正常' COMMENT '状态',
  `cover_image` text,
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `visibility` varchar(20) NOT NULL DEFAULT 'public',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content`
--

LOCK TABLES `content` WRITE;
/*!40000 ALTER TABLE `content` DISABLE KEYS */;
INSERT INTO `content` VALUES (1,1,'三国人物志-戏志才','<p><br><br>烛影在戏志才的病榻前幽幽摇曳，宛若一缕将烬的残念。他枯瘦的手指在锦衾上微微颤动，仿佛仍在推演一局未终的棋。窗外更鼓声隐约传来，夜已三更。<br><br>曹操握着那双渐渐凉去的手，忽然想起初逢之日——那人青衣布履，独立于颍川的杏花树下，袖中竹简滑落，其上墨痕犹湿。那时他说：“明公欲得的天下，不在马蹄所踏的疆土，而在人心所向的晨光。”而今，这双手再不能为他展图指画，点染山河经纬了。<br><br>案头汤药渐冷，白汽散尽。戏志才忽然睁开双眼，眸中竟掠过一霎清辉。他望向帐顶悬垂的铜铸星图，嘴角浮起一丝极淡的笑意：“二十八宿之位……又偏了三度。”语罢，眼中光亮如潮水缓缓退去，只余下深潭般的寂然。<br><br>曹操觉出掌中之手终于沉落。他默然静坐，直至晨光浸透帐幔，才轻轻将那已冰凉的手放回衾中。起身时，忽见枕畔露出一角泛黄的绢帛，上面是戏志才病中勉力勾画的兖州水利图——墨迹虽已虚浮散漫，河道走向却依然清晰如生命的掌纹。戏志才的病榻前，烛火摇曳如将熄的残念。他枯瘦的手指在锦被上微微颤动，仿佛仍在推演着某局未竟的棋。窗外传来隐约的更鼓声，三更天了。<br><br>曹操握着那双渐凉的手，忽然想起初遇时，这人青衣布履站在颍川的杏花树下，袖中落出的竹简上墨迹未干。那时他说：“明公要的天下，不在马蹄踏过的疆土，而在人心归处的晨光。”如今这双手再不能为他展开地图，指点山河经纬了。<br><br>侍从端来的汤药在案头渐渐失了热气。戏志才忽然睁开眼，眸子里竟有一瞬清亮，他望向帐顶悬挂的铜制星图，嘴角牵起极淡的弧度：“二十八宿的位置……又偏移了三度。”说完这句，他眼中的光便如退潮般缓缓散去，只余下深潭似的平静。<br><br>曹操感到掌中的手彻底沉了下去。他静坐良久，直到晨光染白帐幔，才轻轻将那已冰凉的手放回衾被之中。起身时，他看见枕边露出一角泛黄的绢帛，上面是戏志才最后勾勒的兖州水利图——墨迹在病中已显得虚浮，但河道走向依然清晰如掌纹。</p>','散文','正常','http://localhost:9090/uploads/1017fdbb123a479e873b58d7c798e33b.png',28,0,'2026-03-09 02:38:54','2026-03-09 02:38:54','public'),(3,2,'法力无边高大仙','<p style=\"text-align: start;\"> 腾蛇山，飞马集。19</p><p style=\"text-align: start;\"> 靠墙躺着的高贤腿微微一抖，人猛的从噩梦中惊醒过来。34</p><p style=\"text-align: start;\"> 最先映入他眼帘的就是造型有些怪异黄铜丹炉，足有一米高，外形如同没有壶嘴的水壶，有三个蛤蟆腿状支腿，下面是砖砌的台座。13</p><p style=\"text-align: start;\"> 台座下方有生火的通道，并有烟道连接烟囱。更准确说这是一个结构有点复杂的柴火灶台。</p><p style=\"text-align: start;\"> 高贤呆了一会才叹口气，这个造型有点奇葩的炼丹炉，是他吃饭的家伙。1</p><p style=\"text-align: start;\"> 已经是他穿越的第三天了，他适应了新身体和新名字，却没能适应自己新身份：炼丹师。6</p><p style=\"text-align: start;\"> 高贤伸出左手心中默念：“鉴来。”17</p><p style=\"text-align: start;\"> 他手里凭空多出一面造型古雅的青铜镜，铜镜把手上刻着四个字：风月宝鉴。37</p><p style=\"text-align: start;\"> 这面风月宝鉴算是《红楼梦》的周边，是朋友送他的生日礼物，不知怎么的和他一起穿越了。17</p><p style=\"text-align: start;\"> 此刻，光洁镜面上映照出一张英俊年轻的脸。6</p><p style=\"text-align: start;\"> 高贤平时也就读读水浒同人，看看小成本电影，也想不到什么合适词汇形容自己现在这副长相。33</p><p style=\"text-align: start;\"> 什么剑眉星目，面若冠玉，这些都太空洞了。4</p><p style=\"text-align: start;\"> 只能说有点像年轻的某个男影星，帅的端正又英气。尤其黑色眼睛，晶晶亮的好像能闪光……137</p><p style=\"text-align: start;\"> 那感觉就像有专业灯光师在对面给打光一样！</p><p style=\"text-align: start;\"> 唯一的缺点就是脸色苍白，黑眼圈，看着不太健康。12</p><p style=\"text-align: start;\"> “老子要有这张脸，能单身三十多年！”46</p><p style=\"text-align: start;\"> 高贤有些自恋的摸着自己脸，想到只能依靠勤劳双手的日子，满是感叹。16</p><p style=\"text-align: start;\"> 这是他对穿越唯一满意的地方，充满活力的年轻身体，充满希望的英俊面孔！3</p><p style=\"text-align: start;\"> 风月宝鉴镜面上浮现出了一行行字迹。</p><p style=\"text-align: start;\"> 高贤：寿命24/6643</p><p style=\"text-align: start;\"> 修为：练气二层（2/200）7</p><p style=\"text-align: start;\"> 五行功第二层熟练（15/200）2</p><p style=\"text-align: start;\"> 御火术入门（88/100）1</p><p style=\"text-align: start;\"> 御水术入门（80/100）1</p><p style=\"text-align: start;\"> 清洁术入门（95/100）2</p><p style=\"text-align: start;\"> 飞羽术熟练（99/200）1</p><p style=\"text-align: start;\"> 清风剑法入门(33/100)2</p><p style=\"text-align: start;\"> 一阶炼丹术熟练（29/1000）1</p><p style=\"text-align: start;\"> 回气丹熟练（55/200）1</p><p style=\"text-align: start;\"> 固元丹熟练（41/200）1</p><p style=\"text-align: start;\"> 白露丹熟练（11/200）11</p><p style=\"text-align: start;\"> 高贤研究了两天，基本搞清楚了风月宝鉴用处。7</p><p style=\"text-align: start;\"> 这镜子能照出他个人状态，把各种能力数据化。问题是他炼丹术处在灰色状态，无法使用。7</p><p style=\"text-align: start;\"> 高贤尝试了许多办法，都无法激活炼丹术，也无法改变镜子里面的数据。</p><p style=\"text-align: start;\"> 按照原主留下的记忆，他是药铺的炼丹师！</p><p style=\"text-align: start;\"> 药铺提供药材，他提供丹药。</p><p style=\"text-align: start;\"> 他现在欠药铺回气丹、固元丹、白露丹各五百颗。7</p><p style=\"text-align: start;\"> 他还有个非常大的麻烦：本主把炼制的一批丹药偷偷换取赤血丹。</p><p style=\"text-align: start;\"> 赤血丹功效很霸道的一种练气丹药，原主服用赤血丹突破到练气二层，结果不知怎么就挂了，把身体留给了他。7</p><p style=\"text-align: start;\"> 炼丹消耗的大批药材成了巨大亏空。他怎么向药铺老板交代？</p><p style=\"text-align: start;\"> 更严重的问题是，原主的记忆残缺不全，炼丹术相关的记忆也是如此。</p><p style=\"text-align: start;\"> 就是药材足够，他现在也无法炼制丹药。</p><p style=\"text-align: start;\"> 算算时间，五天前他就该交第一批丹药了。</p><p style=\"text-align: start;\"> 他不知道交不出丹药会有什么后果，想来不会有什么好果子吃！4</p><p style=\"text-align: start;\"> 高贤正琢磨办法的时候，就听到院门哐当一声被推开，他一个激灵，急忙凑到窗边向外看。</p><p style=\"text-align: start;\"> 透过窗纸上裂开的缝隙，高贤看到矮胖如猪的朱掌柜，看到了朱掌柜身后的人高胸高的老板娘朱七娘。15</p><p style=\"text-align: start;\"> 两人站在一起，身高差了快两尺了，一个胖，一个高，这搭配让人印象异常深刻。也让高贤一眼认出了对方身份。</p><p style=\"text-align: start;\"> 高贤叹口气拽了拽身上满是皱褶的道袍，他深呼了一口气后推门迎上去。</p><p style=\"text-align: start;\"> “朱大哥、嫂子、快屋里坐……”6</p><p style=\"text-align: start;\"> 高贤拿出的中年社畜历练二十年的职业微笑，客气又热情招呼着对方。3</p><p style=\"text-align: start;\"> 小高，你搞的怎么样了？”肥头大耳朱掌柜笑呵呵的问道。2</p><p style=\"text-align: start;\"> 高贤一脸不好意思的说道：“朱大哥，我急着突破到练气二层，丹药还没炼好，等弄好了我立即给您送过去。”</p><p style=\"text-align: start;\"> 朱掌柜笑了笑道：“小高，修炼的事情要循序渐进，千万不能着急。一个走火入魔可是会出人命的。”2</p><p style=\"text-align: start;\"> 一旁的老板娘脸色冷硬，她没说话，可她只是沉着脸站在那就很有压迫感。1</p><p style=\"text-align: start;\"> 高贤一阵心虚，他这身体其实已经不矮了，按他看怎么也有一米八五还多。站在老板娘面前却矮了小半个头。12</p><p style=\"text-align: start;\"> 他记忆里这位老板娘可是练气九层，极其擅长打断人骨头，武力值爆表。9</p><p style=\"text-align: start;\"> 不过，这位老板娘长眉深目，五官立体，小麦色肌肤紧致光润，宽松蓝色道袍也遮掩不住的高胸长腿。15</p><p style=\"text-align: start;\"> 她五官轮廓深邃，长眉微挑，碧绿瞳孔宛如宛如猫眼宝石非常的漂亮。</p><p style=\"text-align: start;\"> 近距离打量，高贤才发现老板娘很好看。这遍地黄土的地方居然有这般美女！</p><p style=\"text-align: start;\"> 出于男人的本能，出于三十多年闷骚的本性，他忍不住多看了一眼。1</p><p style=\"text-align: start;\"> 老板娘微微皱眉，漂亮的碧绿眼眸微微转动，给了高贤一个意义不明的眼神。9</p><p style=\"text-align: start;\"> “老板娘这是什么意思？”13</p><p style=\"text-align: start;\"> 高贤没看懂这眼神的意思，却不影响他乖巧服软，他一脸谦卑解释道：“朱大哥、我急着突破修为，也是为了更好的炼丹。</p><p style=\"text-align: start;\"> “修为越高，对火候把控的就越好，炼出丹药的品质也就越高。”</p><p style=\"text-align: start;\"> 高贤赔笑：“朱大哥，嫂子，我也不是故意耽误。宽限几天，宽限几天。”</p><p style=\"text-align: start;\"> 朱掌柜摆摆手道：“就算不看你师父的面子，耽误几天也没什么的。”</p><p style=\"text-align: start;\"> 他胖脸上满是和和气气笑容，似乎这根本不算什么事。</p><p style=\"text-align: start;\"> “再给你十天时间够不够？”</p><p style=\"text-align: start;\"> 朱掌柜这么好说话，也让高贤松口气。</p><p style=\"text-align: start;\"> 他赔笑点头，“好好，十天够了，炼好了就给您送过去。”</p><p style=\"text-align: start;\"> “好好炼丹，别辜负了你师父对你的期望。”</p><p style=\"text-align: start;\"> 朱掌柜拍了下高贤肩膀勉励了两句，一副长辈关怀晚辈的架势。3</p><p style=\"text-align: start;\"> “生活上有什么问题，都可以和我说……行了，我还有事就先走了。你不用送了。”3</p><p style=\"text-align: start;\"> 高贤急忙跟上去送客，朱掌柜头笑吟吟出了大门，对高贤表现的颇为客气。</p><p style=\"text-align: start;\"> 跟在朱掌柜后面朱七娘，临走的时候却深深看了眼高贤。</p><p style=\"text-align: start;\"> 那眼神似乎颇有些复杂的意味……3</p><p style=\"text-align: start;\"> 高贤不明所以，嫂子这是、看上他了？9</p><p style=\"text-align: start;\"> 俗话说好吃不过饺子，可咱不是那样滴人啊！10</p><p style=\"text-align: start;\"> 不管心里怎么想，高贤表面上还是很恭敬稽首施礼，把朱掌柜夫妇恭送出门。</p>','散文','正常','https://tse4-mm.cn.bing.net/th/id/OIP-C.5Ylh06G7Fodb5Ihsf5JN5AHaNK?w=133&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3',13,0,'2026-03-09 16:11:50','2026-03-09 16:11:50','public'),(4,2,'序列大明','<p style=\"text-align: start;\">当第一滴夜雨从天空坠落，位于大明帝国西南地域的成都府终于开始苏醒。45</p><p style=\"text-align: start;\"> 被放置在摩天大厦顶端的全息广告发射器几乎在同一时开启。21</p><p style=\"text-align: start;\"> 各色绚丽的霓虹炫光喷涌而出，有如法相般巨大的身影冲天而起，托住逐渐昏暗的天穹。4</p><p style=\"text-align: start;\"> “一颗金丹吞入腹，无需义体也长生！”107</p><p style=\"text-align: start;\"> 笑容温和的老道手捧着最新上市的原生肉体延寿金丹。3</p><p style=\"text-align: start;\"> 道袍上一头飘逸的仙鹤盘绕碧绿青峰不停游走，片刻化为一副太极阴阳图案，周而复始，循环不止。18</p><p style=\"text-align: start;\"> 青峰衍太极——这是成都府本地最大道门寡头青城集团的标志。99</p><p style=\"text-align: start;\"> 老道身影极其庞大，将其他公司的全息投影全部压在拂尘之下，尽显道门气魄。3</p><p style=\"text-align: start;\"> 在这片区域唯一能跟其争锋的，只有成都府教坊司最近风头无两的头牌花魁，杜十三娘。43</p><p style=\"text-align: start;\"> 如瀑长发香肩半露，神态妩媚如痴如醉。6</p><p style=\"text-align: start;\"> “夜合之资，原生一千，仿生三百。全息黄粱美梦只需一百大明宝钞。”70</p><p style=\"text-align: start;\"> 闪动着甜腻粉光的宣传词旁边，还投射着一个巨大的“耍”字！101</p><p style=\"text-align: start;\"> 极具川蜀风格的宣传语在生动肉体的映衬下，显得格外有诱惑力。10</p><p style=\"text-align: start;\"> 一艘印着‘崇祯中兴’的巡逻飞艇从两道投影鼻间的缝隙滑过。42</p><p style=\"text-align: start;\"> 全副武装的天府戍卫站在吊舱内，瞪着猩红的电子眼眸俯视着下方灯光渐起的街道。21</p><p style=\"text-align: start;\"> 此时虽然刚刚天黑，可位于鸡鹅区罪民街黄金地段的无事酒肆却已经是人满为患。14</p><p style=\"text-align: start;\"> “菊花古剑和酒，被咖啡泡入喧嚣的亭院。”29</p><p style=\"text-align: start;\"> “异族在日坛膜拜古人月亮，崇祯盛世令人神往。”16</p><p style=\"text-align: start;\"> 天花板上挂着一对老板不知道从哪儿淘来的复古音响，放着盛明乐队最新单曲《梦回崇祯》。48</p><p style=\"text-align: start;\"> 低沉沙哑的男声回荡在酒肆内，旋转的灯球放射出腻人的粉色与炫目的蓝光。1</p><p style=\"text-align: start;\"> 空气中机油和酒精的味道交织混杂，撩拨着最原始的欲望。10</p><p style=\"text-align: start;\"> 正当气氛渐热的时候，酒肆的店门突然被人猛地推开。</p><p style=\"text-align: start;\"> 冰冷潮湿的寒风倒灌进来，靠近门口的客人浑身一颤，纷纷转头怒视那道钉在门口的人影。9</p><p style=\"text-align: start;\"> 黑色的雨伞被收拢，露出一张棱角分明的俊朗面孔。2</p><p style=\"text-align: start;\"> 男人留着一头干净利落的短发，身上穿着一件改良短款直裰明服，裸露在衣服外的皮肤看不出半点义肢改造的痕迹。17</p><p style=\"text-align: start;\"> 随着男人转动眼神扫视四周，被衣领挡住的脖间隐约可见一副刺青凶兽。2</p><p style=\"text-align: start;\"> 异兽身形如同一头长了龙角的豺狼，嘴里衔着一柄宝剑，兽目如火，睥睨霸道。47</p><p style=\"text-align: start;\"> “他妈的，刚来点兴致就吹大爷我一身冷风，你是不是想找死.....”1</p><p style=\"text-align: start;\"> 一名喝的酩酊大醉的汉子摇摇晃晃站了起来，嘴里骂骂咧咧。</p><p style=\"text-align: start;\"> 他话还没说完，一旁的同伴却突然暴起，极其粗暴的将他按倒在桌上。</p><p style=\"text-align: start;\"> 不明所以的汉子正准备要发怒，耳边却传来同伴紧张的低吼声：</p><p style=\"text-align: start;\"> “没看那人脖子上刺的什么啊？他妈的袍哥会的人你也敢惹，你想找死别拉上我们！”52</p><p style=\"text-align: start;\"> 脖刺睚眦，这是成都府明人黑帮袍哥会中“浑水”一脉的标志。29</p><p style=\"text-align: start;\"> 醉汉闻言浑身一颤，一身酒意立马醒了七八分，脸朝下贴着酒桌，像只鹌鹑一样慢慢缩回自己的椅子中。3</p><p style=\"text-align: start;\"> 周围的酒客也默契的挪开眼神，假装一切无事发生。</p><p style=\"text-align: start;\"> 男人没有理会发生的小插曲，径直朝着酒肆角落的一个偏僻卡座走去。</p><p style=\"text-align: start;\"> 卡座里，一个肥头大耳的胖子正卧在沙发中，嘴角叼着一根市面上极为罕见的纸质卷烟，满脸惬意的吞云吐雾。12</p><p style=\"text-align: start;\"> “寇哥，你找我？”5</p><p style=\"text-align: start;\"> 听到男人的声音，胖子满是褶子的脸上慢慢裂开一条不易察觉的缝隙，露出两粒黑色的眼珠。11</p><p style=\"text-align: start;\"> “来了啊，快坐。”1</p><p style=\"text-align: start;\"> 胖子笑着坐起身子，抬手将桌上一盏与周围环境格格不入的酒碗推了过去。1</p><p style=\"text-align: start;\"> “剑南烧春，地道的明酒，李钧你尝尝。”59</p><p style=\"text-align: start;\"> 李钧端起酒盏一饮而尽，顿时一条火线从喉间杀到胃中，将身上的寒意烧的一干二净。6</p><p style=\"text-align: start;\"> “好酒，”李钧抹了下嘴巴，笑问道：“寇哥你找我来，不会就是单纯想请我喝酒吧？”6</p><p style=\"text-align: start;\"> 余寇并没有直接回答李钧的问题，低着头用肥大的手掌把玩着那枚酒盏，自顾自说道：2</p><p style=\"text-align: start;\"> “如今咱们成都府的酒肆只知道那些外邦番子的酒要用高脚杯，都忘了咱们大明帝国的酒要用酒碗来喝才地道。”24</p><p style=\"text-align: start;\"> “什么样的酒就该配什么样的碗，什么样的身份就该办什么样的事。”39</p><p style=\"text-align: start;\"> 余寇抬头，笑眯眯道：“这个道理你明白吗？”1</p><p style=\"text-align: start;\"> 李钧瞳孔颤了一下，脸色依旧如常，点头道：“明白。”2</p><p style=\"text-align: start;\"> 余寇两指扣着金属桌面，发出铿锵的清脆声响，“既然是个懂道理的人，那为什么一个月不往处里传消息，你是不是忘了自己是什么身份？”6</p><p style=\"text-align: start;\"> 是什么身份？我他妈身份多了。17</p><p style=\"text-align: start;\"> 李钧心头忍不住暗骂一句。</p><p style=\"text-align: start;\"> 自己是穿越者，莫名其妙来到大明国祚万寿无疆的吊诡世界也就算了，还是他妈的一名二五仔！74</p><p style=\"text-align: start;\"> 明面上的身份是黑帮袍哥会的成员，暗地里还是成都府锦衣卫二处的线人！52</p><p style=\"text-align: start;\"> 这个开局是李钧没料到的。</p><p style=\"text-align: start;\"> 李钧吐出一口浊气，笑道：“大人你误会了，我不往处里传消息是因为赵鼎那边最近很老实，除了常规的走私贩卖违禁品外，没搞过什么大动作。”18</p><p style=\"text-align: start;\"> 李钧口中的赵鼎，正是成都府浑水袍哥的舵把子，龙头老大。</p><p style=\"text-align: start;\"> “没动作？”</p><p style=\"text-align: start;\"> 余寇嗤笑一声，五指猛然合拢，手中酒盏“啪”的一声被捏成碎片。</p><p style=\"text-align: start;\"> “就在你进店前十分钟，祭刀会旗下的一个歌舞伎町才被袭击。中层若头级干部，祭刀会‘十贵’之一的流川坦被人掳走，你敢说这事情跟你没关系？”43</p><p style=\"text-align: start;\"> 李钧心头一凛，默不作声的用食指刮了刮自己的眉毛。4</p><p style=\"text-align: start;\"> 这胖子的狗鼻子还真灵啊！</p><p style=\"text-align: start;\"> 余寇冷冷一笑，“流川坦可是祭刀会的接班人之一，赵鼎动他是不是想跟那群倭寇罪民开战？”</p><p style=\"text-align: start;\"> 罪民这个称呼始于隆武帝朱平渊时期，他下旨撤销了藩属国律令，悍然发动扩张战争。68</p><p style=\"text-align: start;\"> 力排众议在所有战败国设立罪民区，将其中的百姓划定为罪民，纳入整个帝国阶级的最底层。86</p><p style=\"text-align: start;\"> 祭刀会和安南帮，就是成都府最大的两个罪民帮派。22</p><p style=\"text-align: start;\"> “袍哥会哪儿来的胆子开战啊，”李钧摊手笑道：“不过是儿子打架输了喊爸爸。”6</p><p style=\"text-align: start;\"> “赵斗因为争地盘的事情在流川坦手上吃了不少亏，已经成了袍哥会里的笑柄，赵鼎让我帮他出头而已。”1</p><p style=\"text-align: start;\"> 李钧耸了耸肩膀说道：“大人你也知道，赵鼎无后，只有赵斗这么一个侄子。他要是再不帮这位太子爷挽回点脸面，赵斗拿什么去接他的班。”5</p><p style=\"text-align: start;\"> “赵鼎真是越活越回去了，小辈子打架他插手像什么话，不过...”</p><p style=\"text-align: start;\"> 余寇不屑的撇了撇嘴，突然眼神一凝，肥胖的身躯朝前倾靠几分。</p><p style=\"text-align: start;\"> 李钧顿时感觉到一股强烈的压迫感撞向自己，跳动的心脏都慢了两拍，身体不受控制的绷紧。</p><p style=\"text-align: start;\"> 这就是序列的力量吗....63</p><p style=\"text-align: start;\"> “以后就算是这种小事，我也不想从别人的口中听到，你明白我的意思吗？”</p><p style=\"text-align: start;\"> 李钧忍住心底的躁动不安，将一缕杀意死死按耐住，言辞恳切道：“大人放心，以后不会了。”16</p><p style=\"text-align: start;\"> 余寇脸上缓缓露出满意的笑容，他知道李钧心中有怒，可他根本不在乎。</p><p style=\"text-align: start;\"> 谁会在意一件工具的喜怒哀乐？11</p><p style=\"text-align: start;\"> 卡座内一时陷入沉默，片刻后李钧还是忍不住将心头一直困扰他的疑惑问了出来。</p><p style=\"text-align: start;\"> “大人，既然锦衣卫想要收拾赵鼎，那随便找个借口拔了他就是，何必这么麻烦？”</p><p style=\"text-align: start;\"> 打了一棍子，那就要给一颗枣。</p><p style=\"text-align: start;\"> 才敲打完李钧的余寇显得很有耐心，解释道：</p><p style=\"text-align: start;\"> “一座城市就算再干净，下水道里也会有耗子。”2</p><p style=\"text-align: start;\"> “袍哥会死了一个赵鼎，马上就有吴鼎、李鼎顶上来。像这种野草根本除不了根，春风一吹，立马又生出来。”28</p><p style=\"text-align: start;\"> 李钧有些不解：“既然这样，那我何必监视赵鼎？”12</p><p style=\"text-align: start;\"> “这些事情你现在不用知道。”</p><p style=\"text-align: start;\"> 酒肆光影摇动，余寇一张胖脸藏在阴影之中，那双嵌在缝隙中的眸子却异常明亮。</p><p style=\"text-align: start;\"> “你现在就好好给我盯着赵鼎，这老头已经到了狗急跳墙的地步了。”1</p><p style=\"text-align: start;\"> 余寇语调变柔，带着一股让人信赖的真诚：“等做完这件事，我会向成都府锦衣卫户所给你申请赏赐。你不是想要走武序吗？这是你破锁晋序最好的机会。”21</p><p style=\"text-align: start;\"> “到时候你就能脱离贱民籍贯，甚至还能加入锦衣卫，这可是千金难求的好机会。”11</p><p style=\"text-align: start;\"> “多谢大人。”</p><p style=\"text-align: start;\"> 余寇画饼的功夫显然不够熟练，李钧心底根本毫无波动，只是用生硬的语调附和着。11</p><p style=\"text-align: start;\"> “你也别有什么顾虑，你是锦衣卫线人的事情除了我以外，没有其他人知道。大胆的放开手脚干，我等你的好消息。”51</p><p style=\"text-align: start;\"> 余寇这句话像一条阴冷的毒蛇缠上李钧的身体，他放在膝盖上的双手骤然紧握成拳，低垂的眼眸中散发着彻骨的寒意。9</p><p style=\"text-align: start;\"> “那我可要多谢大人您的提携了啊！”1</p><p style=\"text-align: start;\"> 余寇自然听出了对方话中的那股不满和愤懑，但也只是不以为意的笑了笑，摆手道：“行了，都是一家人何必这么客气，忙你的事情去吧。”</p><p style=\"text-align: start;\"> 李钧沉默起身，抱拳拱手之后，转身大步往外走去。</p><p style=\"text-align: start;\"> 余寇看着对方离去的背影，嘴角徐徐露出一丝轻蔑的笑意。</p><p style=\"text-align: start;\"> 他重新卧进沙发中，抬手打了一个响指，立马有两名衣着暴露的仿生人走了过来，一左一右依偎在他身旁。48</p><p style=\"text-align: start;\"> 随着卡座帷帘缓缓合上，回荡在酒肆里的歌声越发激烈高亢。</p><p style=\"text-align: start;\"> “沿着掌纹烙着宿命，今宵梦醒无酒。沿着宿命走入迷思，梦里回到崇祯！”42</p><p style=\"text-align: start;\"> 李钧一脚踹开酒肆大门，径直闯入那片连绵的雨幕，大步离去</p>','散文','正常','https://bookcover.yuewen.com/qdbimg/349573/1037075059/600.webp,http://localhost:9090/uploads/ab2dfb534e0f4f0d88ad6fabd0744a26.jpeg,http://localhost:9090/uploads/a8469017478b48719714a78ebbfeae43.jpg,https://bookcover.yuewen.com/qdbimg/349573/1043800607/180.webp',7,0,'2026-03-09 16:15:45','2026-03-09 16:15:45','public'),(7,2,'22','<p><br></p>','小说','草稿','',0,0,'2026-03-09 16:33:53','2026-03-09 16:33:53','public'),(8,2,'1','<p>1</p>','散文','草稿','http://localhost:9090/uploads/6481e46401f34f2194a57512a60ad3ea.jpeg,http://localhost:9090/uploads/4bfd753e8d664e0fbcdfa4a69d377209.jpeg,http://localhost:9090/uploads/5d1652e0251d4aebac0c042284c875d6.jpeg',0,0,'2026-03-09 16:34:32','2026-03-09 16:34:32','public');
/*!40000 ALTER TABLE `content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content_tag`
--

DROP TABLE IF EXISTS `content_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_tag` (`content_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content_tag`
--

LOCK TABLES `content_tag` WRITE;
/*!40000 ALTER TABLE `content_tag` DISABLE KEYS */;
INSERT INTO `content_tag` VALUES (2,1,3),(4,3,18);
/*!40000 ALTER TABLE `content_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content_view_log`
--

DROP TABLE IF EXISTS `content_view_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content_view_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `view_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_view_time` (`view_time`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容访问日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content_view_log`
--

LOCK TABLES `content_view_log` WRITE;
/*!40000 ALTER TABLE `content_view_log` DISABLE KEYS */;
INSERT INTO `content_view_log` VALUES (1,1,'2026-03-09 02:38:54'),(2,1,'2026-03-09 02:39:21'),(3,1,'2026-03-09 02:46:01'),(4,1,'2026-03-09 02:46:19'),(5,1,'2026-03-09 02:49:15'),(6,2,'2026-03-09 02:55:31'),(7,2,'2026-03-09 02:55:37'),(8,2,'2026-03-09 02:56:07'),(9,1,'2026-03-09 02:56:12'),(10,2,'2026-03-09 02:56:27'),(11,2,'2026-03-09 02:56:31'),(12,2,'2026-03-09 02:57:23'),(13,2,'2026-03-09 03:01:03'),(14,2,'2026-03-09 03:07:36'),(15,1,'2026-03-09 03:07:40'),(16,1,'2026-03-09 03:12:35'),(17,1,'2026-03-09 03:18:08'),(18,2,'2026-03-09 16:02:25'),(19,1,'2026-03-09 16:02:29'),(20,2,'2026-03-09 16:02:40'),(21,2,'2026-03-09 16:04:01'),(22,1,'2026-03-09 16:04:03'),(23,1,'2026-03-09 16:04:22'),(24,1,'2026-03-09 16:04:25'),(25,2,'2026-03-09 16:05:42'),(26,1,'2026-03-09 16:05:45'),(27,1,'2026-03-09 16:05:46'),(28,1,'2026-03-09 16:05:57'),(29,3,'2026-03-09 16:11:50'),(30,3,'2026-03-09 16:12:37'),(31,3,'2026-03-09 16:12:38'),(32,3,'2026-03-09 16:13:03'),(33,3,'2026-03-09 16:13:05'),(34,3,'2026-03-09 16:14:04'),(35,4,'2026-03-09 16:15:46'),(36,4,'2026-03-09 16:15:52'),(37,5,'2026-03-09 16:31:44'),(38,5,'2026-03-09 16:31:49'),(39,4,'2026-03-09 16:32:03'),(40,5,'2026-03-09 16:33:11'),(41,4,'2026-03-09 16:33:14'),(42,4,'2026-03-09 16:34:13'),(43,4,'2026-03-09 16:34:17'),(44,5,'2026-03-09 16:35:44'),(45,5,'2026-03-09 16:41:24'),(46,9,'2026-03-09 16:41:34'),(47,9,'2026-03-09 16:41:36'),(48,9,'2026-03-09 16:41:41'),(49,9,'2026-03-09 16:41:49'),(50,3,'2026-03-09 16:45:11'),(51,1,'2026-03-09 16:46:52'),(52,1,'2026-03-09 16:46:59'),(53,4,'2026-03-09 16:47:03'),(54,5,'2026-03-09 16:47:18'),(55,5,'2026-03-09 16:47:59'),(56,9,'2026-03-09 16:48:05'),(57,9,'2026-03-09 16:48:17'),(58,1,'2026-03-09 16:48:25'),(59,1,'2026-03-09 16:48:27'),(60,3,'2026-03-09 16:48:28'),(61,5,'2026-03-09 16:48:29'),(62,9,'2026-03-09 16:48:35'),(63,9,'2026-03-09 16:48:49'),(64,9,'2026-03-09 16:48:58'),(65,9,'2026-03-09 16:49:10'),(66,1,'2026-03-09 16:50:17'),(67,9,'2026-03-09 16:54:16'),(68,9,'2026-03-09 16:54:22'),(69,9,'2026-03-09 16:54:24'),(70,9,'2026-03-09 16:54:30'),(71,9,'2026-03-09 16:54:35'),(72,5,'2026-03-09 16:54:58'),(73,3,'2026-03-09 16:55:03'),(74,5,'2026-03-09 16:55:05'),(75,5,'2026-03-09 16:55:07'),(76,9,'2026-03-09 16:55:11'),(77,9,'2026-03-09 16:55:13'),(78,3,'2026-03-09 16:55:15'),(79,1,'2026-03-09 16:55:18'),(80,1,'2026-03-09 16:55:19'),(81,1,'2026-03-09 16:55:35'),(82,1,'2026-03-09 17:34:59'),(83,9,'2026-03-09 17:35:05'),(84,9,'2026-03-09 17:36:34'),(85,1,'2026-03-09 17:36:42'),(86,9,'2026-03-09 17:36:56'),(87,9,'2026-03-09 17:37:20'),(88,9,'2026-03-09 17:38:03'),(89,9,'2026-03-09 17:38:07'),(90,9,'2026-03-09 17:38:10'),(91,9,'2026-03-09 17:38:14'),(92,9,'2026-03-09 17:38:16'),(93,9,'2026-03-09 17:38:50'),(94,9,'2026-03-09 17:38:54'),(95,9,'2026-03-09 17:38:57'),(96,9,'2026-03-09 17:39:02'),(97,5,'2026-03-09 17:52:08'),(98,5,'2026-03-09 17:52:22'),(99,1,'2026-03-09 17:59:15'),(100,1,'2026-03-09 17:59:20'),(101,5,'2026-03-09 17:59:23'),(102,5,'2026-03-09 17:59:32'),(103,5,'2026-03-09 18:04:01'),(104,9,'2026-03-09 18:04:17'),(105,9,'2026-03-09 18:06:24'),(106,3,'2026-03-09 18:06:39'),(107,3,'2026-03-09 18:06:45'),(108,3,'2026-03-09 18:06:47'),(109,5,'2026-03-09 18:08:28'),(110,9,'2026-03-09 18:08:33');
/*!40000 ALTER TABLE `content_view_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `email_code`
--

DROP TABLE IF EXISTS `email_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_code` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL COMMENT '目标邮箱',
  `code` varchar(10) NOT NULL COMMENT '验证码',
  `purpose` varchar(20) NOT NULL DEFAULT 'bind' COMMENT '用途: bind/change',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_email_purpose` (`email`,`purpose`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邮箱验证码表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_code`
--

LOCK TABLES `email_code` WRITE;
/*!40000 ALTER TABLE `email_code` DISABLE KEYS */;
/*!40000 ALTER TABLE `email_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_content` (`user_id`,`content_id`),
  KEY `idx_content_id` (`content_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
INSERT INTO `favorite` VALUES (2,3,9,'2026-03-09 17:38:15');
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tag_name` varchar(50) NOT NULL COMMENT '标签名',
  `type` enum('system','custom') NOT NULL DEFAULT 'system' COMMENT '类型',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人(自定义标签时有值)',
  `status` enum('正常','禁用') NOT NULL DEFAULT '正常' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
INSERT INTO `tag` VALUES (2,'青春','system',NULL,'正常','2026-02-23 00:17:11'),(3,'历史','system',NULL,'正常','2026-02-23 00:17:22'),(4,'文学','system',NULL,'正常','2026-02-23 00:17:27'),(5,'爱情','system',NULL,'正常','2026-02-23 00:18:59'),(6,'亲情','system',NULL,'正常','2026-02-23 00:18:59'),(7,'友情','system',NULL,'正常','2026-02-23 00:18:59'),(8,'励志','system',NULL,'正常','2026-02-23 00:18:59'),(9,'人生','system',NULL,'正常','2026-02-23 00:18:59'),(10,'哲思','system',NULL,'正常','2026-02-23 00:18:59'),(11,'乡愁','system',NULL,'正常','2026-02-23 00:18:59'),(12,'自然','system',NULL,'正常','2026-02-23 00:18:59'),(13,'旅行','system',NULL,'正常','2026-02-23 00:18:59'),(14,'都市','system',NULL,'正常','2026-02-23 00:18:59'),(15,'校园','system',NULL,'正常','2026-02-23 00:18:59'),(16,'职场','system',NULL,'正常','2026-02-23 00:18:59'),(17,'悬疑','system',NULL,'正常','2026-02-23 00:18:59'),(18,'奇幻','system',NULL,'正常','2026-02-23 00:18:59'),(19,'武侠','system',NULL,'正常','2026-02-23 00:18:59'),(20,'战争','system',NULL,'正常','2026-02-23 00:18:59'),(21,'古典','system',NULL,'正常','2026-02-23 00:18:59'),(22,'现代','system',NULL,'正常','2026-02-23 00:18:59');
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `role` enum('user','admin') NOT NULL DEFAULT 'user' COMMENT '角色',
  `status` enum('正常','禁用') NOT NULL DEFAULT '正常' COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `email` varchar(100) DEFAULT NULL COMMENT '绑定邮箱',
  `bio` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$Ng257mU0hmYVZpYcMqtIoeYxz7AembOQw4nbwMel5013WY3RePaGG',NULL,'admin','正常','2026-02-23 00:14:51',NULL,NULL),(2,'test','$2a$10$JnojM0.xFXuptHzY6OBlYuU7EFtZpRtganN62CK5waEGIWja/bkHG','http://localhost:9090/uploads/5443f37f2b804cd7a9756448914abd11.jpg','user','正常','2026-03-09 16:07:01',NULL,NULL),(3,'user1','$2a$10$mJqXiKNTtpuxhLS6Ot7J9.lEnwiZ3ABHUUqevbu2jAxwv7JD9F1au',NULL,'user','正常','2026-03-09 17:37:14',NULL,'一个大学生。。。');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_follow`
--

DROP TABLE IF EXISTS `user_follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follower_id` bigint NOT NULL,
  `followee_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`follower_id`,`followee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_follow`
--

LOCK TABLES `user_follow` WRITE;
/*!40000 ALTER TABLE `user_follow` DISABLE KEYS */;
INSERT INTO `user_follow` VALUES (1,2,1,'2026-03-09 17:59:20');
/*!40000 ALTER TABLE `user_follow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_like`
--

DROP TABLE IF EXISTS `user_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content_id` bigint NOT NULL COMMENT '内容ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_like` (`user_id`,`content_id`),
  KEY `idx_like_content_id` (`content_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_like`
--

LOCK TABLES `user_like` WRITE;
/*!40000 ALTER TABLE `user_like` DISABLE KEYS */;
INSERT INTO `user_like` VALUES (2,3,9,'2026-03-09 17:38:03');
/*!40000 ALTER TABLE `user_like` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-09 18:13:02
