SELECT th.id
FROM TbHemocentro th
INNER JOIN TbSolicitacaoDoacao tsd ON th.id = tsd.idHemocentro
WHERE tsd.status IN ('ABERTA', 'EM_ANDAMENTO')
	AND tsd.idTipoSanguineo IN (<tipoSanguineoIdList>)
	AND tsd.idHemocentro IN (<hemocentroIdList>)
	AND tsd.idUsuario != :idUsuario;