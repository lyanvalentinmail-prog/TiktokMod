# Integración con Morphe / Metra

Esta ruta usa los parches de [Metra TikTok Patches](https://github.com/icysymmetra/tiktok-patches-for-morphe) fijados en el commit `8759b348497f39f799baf1bc34015f4b51605b80`. El código de Metra y los cambios de esta carpeta se distribuyen bajo GPL-3.0; consulta [LICENSE](LICENSE) y [NOTICE](NOTICE). El script descarga el archivo fuente oficial y comprueba su SHA-256 antes de aplicarle [nuestros cambios](patches/) y añadir [el fragmento de entrada](overlay/extensions/tiktok/src/main/java/app/morphe/extension/tiktok/settings/preference/ModGatewayFragment.java). El código fuente preparado completo se incluye con el artefacto de compilación.

Esto es independiente del prototipo Xposed de `app/`. No actives ambos sobre la misma instalación: podrían interferir entre sí.

## Qué aporta

- La fuente de Metra declara compatibilidad de desarrollo con TikTok global `com.zhiliaoapp.musically` **47.4.3**. Sus autores indican que las pruebas de ejecución Android de esta revisión están pendientes; ni sus parches ni nuestros cambios se han probado en un teléfono aquí.
- Nuestra modificación cambia el título de la fila de ajustes de Metra a **TikTok Mod**. La implementación base la coloca al principio de Ajustes y privacidad. La entrada normal muestra un único botón **Apariencia**, que abre el menú de Metra, con sus propias categorías y marca. En esa página se muestra información sobre el color de acento y el perfil modo Instagram. Los enlaces directos a secciones y las restauraciones tras reiniciar evitan el nuevo menú inicial.
- Metra contiene los parches **Copy bio and IDs**, **Copy comments without username** y **Open external links directly**. Comprueba que estén seleccionados antes de parchear. Para esos enlaces, nuestro cambio intenta abrir **Chrome** directamente; si no puede hacerlo, conserva la ruta original de Metra hacia el navegador del sistema.
- **Accent color** es un parche opcional de Metra: se elige al parchear, no desde un botón dentro de TikTok. Se debe volver a parchear para cambiarlo. El diseño de perfil estilo Instagram sigue sin implementarse.
- Metra ofrece muchos otros parches, algunos activos por defecto. Revisa la selección y los efectos antes de parchear, especialmente si solo quieres las funciones solicitadas.

## Compilar el paquete de parches

Requisitos: Python 3, `curl`, `patch`, `tar`, `sha256sum`, JDK 21, Android SDK y acceso a los repositorios de dependencias de Gradle y GitHub Packages. La configuración de credenciales para dependencias está descrita en el proyecto de origen; no guardes tokens en este repositorio.

```sh
./scripts/prepare-morphe.sh
cd build/metra-source
./gradlew :patches:buildAndroid --no-daemon
```

El resultado es `build/metra-source/patches/build/release/patches-0.10.0.mpp`, junto con `bundle.sha256`. Es un **paquete de parches**, no un APK de TikTok. El directorio preparado se ignora en Git y el script no sobrescribe uno existente. Para prepararlo de nuevo, elimina únicamente `build/metra-source` después de guardar los cambios que necesites. También puedes usar el flujo de GitHub Actions **Morphe patch bundle**, que intenta compilar y publica el paquete y su código fuente como artefacto; el artefacto solo existe si el flujo termina correctamente.

Para comprobar la preparación sin Android SDK, ejecuta `python3 -m unittest discover -s tests -v` desde la raíz; el test de integración descarga la fuente fijada, salvo que se indique una copia mediante `METRA_ARCHIVE`. Para ejecutar los tests Android específicos de estas funciones:

```sh
cd build/metra-source
./gradlew :extensions:tiktok:testDebugUnitTest --no-daemon \
  --tests app.morphe.extension.tiktok.settings.preference.ModGatewayFragmentTest \
  --tests app.morphe.extension.tiktok.interaction.CopyIdsTest \
  --tests app.morphe.extension.tiktok.comment.CommentCopySanitizerTest \
  --tests app.morphe.extension.tiktok.externalbrowser.ExternalBrowserPatchTest \
  --tests app.morphe.extension.tiktok.externalbrowser.ChromeLinkTest
```

## Usar el paquete

1. Obtén de forma legítima el APK global compatible; este repositorio no lo proporciona. Haz copia de seguridad de los datos locales. Parchear y firmar de nuevo puede impedir actualizar la instalación original y requerir desinstalarla, con posible pérdida de datos.
2. Utiliza [Morphe Desktop](https://github.com/MorpheApp/morphe-cli) con el archivo `.mpp` compilado y tu APK. Por ejemplo, consulta `java -jar morphe-desktop-<versión>-all.jar patch --help` para seleccionar parches y opciones; el formato básico documentado por el proyecto es `java -jar morphe-desktop-<versión>-all.jar patch -p patches-0.10.0.mpp tu_aplicacion.apk`. El ejemplo aplica la selección predeterminada del parcheador, que puede incluir funciones adicionales.
3. Instala el resultado si tu dispositivo lo admite. Abre **Ajustes y privacidad → TikTok Mod → Apariencia** y busca las opciones de la biografía, comentarios y navegador en las categorías de Metra. Es posible que la verificación de integridad, el inicio de sesión o cambios en TikTok impidan su uso.

No se ha publicado un bundle personalizado de este repositorio como fuente remota de Morphe Manager. Agregar la URL de Metra al Manager instalaría el bundle de Metra, **no** este bundle personalizado con la entrada TikTok Mod.
