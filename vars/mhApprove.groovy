/*
Descripcion: Libreria para Aprobar pasajes a ambientes productivos.

  yoiApprove()

  inputs:
    -> env.SIGLA
    -> env.PNAME
    -> env.DEBUG
  outputs:
    <- env.USUARIO
  <- return AUTORIZANTE
**************************************************************/
def call() {
  yoiUtilidades.Messages("Solicitud de Aprobación","title")

  //if( env.DEBUG ) {
  //  yoiUtilidades.Messages("Modo DEBUG activado, se saltea Approval Stage.","warn")
  //}
  //else {
  //  yoiUtilidades.Messages("${env.APPROVER_GROUP}","info")
//envio de hook
    //yoiHookTeams("approve","")
    timeout(time: 5, unit: 'MINUTES') {
      def feedback = input(
        message: "¿Do you approve?",
        ok: 'Continuar',
        submitter: "${env.APPROVER_GROUP}",
        submitterParameter: 'AUTORIZANTE')
        return feedback
        env.USUARIO = feedback['AUTORIZANTE']
        }
   // }
}
