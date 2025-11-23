SELECT * FROM TbHemocentro th
WHERE th.id IN (<hemocentroIdList>)
	AND th.ativo = true;